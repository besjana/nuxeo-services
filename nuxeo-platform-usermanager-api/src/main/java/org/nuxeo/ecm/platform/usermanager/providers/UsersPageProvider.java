package org.nuxeo.ecm.platform.usermanager.providers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.directory.SizeLimitExceededException;
import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * Page provider listing users.
 * <p>
 * This page provider requires two parameters: the first one to
 * be filled with the search string, and the second one to
 * be filled with the selected letter when using the {@code tabbed}
 * listing mode.
 * <p>
 * This page provider requires the property {@link #USERS_LISTING_MODE_PROPERTY} to
 * be filled with a the listing mode to use.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.2
 */
public class UsersPageProvider extends
        AbstractPageProvider<DocumentModel> implements
        PageProvider<DocumentModel> {

    private static final Log log = LogFactory.getLog(UsersPageProvider.class);

    private static final long serialVersionUID = 1L;

    protected static final String USERS_LISTING_MODE_PROPERTY = "usersListingMode";

    protected static final String ALL_MODE = "all";

    protected static final String SEARCH_ONLY_MODE = "search_only";

    protected static final String TABBED_MODE = "tabbed";

    protected static final String SEARCH_OVERFLOW_ERROR_MESSAGE = "label.security.searchOverFlow";

    /**
     * Map with first letter as key and users list as value
     */
    protected Map<String, DocumentModelList> userCatalog;

    protected List<DocumentModel> pageUsers;

    @Override
    public List<DocumentModel> getCurrentPage() {
        if (pageUsers == null) {
            error = null;
            errorMessage = null;
            pageUsers = new ArrayList<DocumentModel>();

            List<DocumentModel> users = new ArrayList<DocumentModel>();
            try {
                UserManager userManager = Framework.getService(UserManager.class);
                String userListingMode = getUserListingMode();
                if (ALL_MODE.equals(userListingMode)) {
                    users = searchAllUsers(userManager);
                } else if (SEARCH_ONLY_MODE.equals(userListingMode)) {
                    users = searchUsers(userManager);
                } else if (TABBED_MODE.equals(userListingMode)) {
                    users = searchUsersFromCatalog(userManager);
                }
            } catch (SizeLimitExceededException slee) {
                error = slee;
                errorMessage = SEARCH_OVERFLOW_ERROR_MESSAGE;
                log.warn(slee.getMessage(), slee);
            } catch (Exception e) {
                error = e;
                errorMessage = e.getMessage();
                log.warn(e.getMessage(), e);
            }

            if (!hasError()) {
                long resultsCount = users.size();
                setResultsCount(resultsCount);
                // post-filter the results "by hand" to handle pagination
                long pageSize = getMinMaxPageSize();
                if (pageSize == 0) {
                    pageUsers.addAll(users);
                } else {
                    // handle offset
                    long offset = getCurrentPageOffset();
                    if (offset <= resultsCount) {
                        for (int i = Long.valueOf(offset).intValue(); i < resultsCount
                                && i < offset + pageSize; i++) {
                            pageUsers.add(users.get(i));
                        }
                    }
                }
            }
        }
        return pageUsers;
    }

    protected String getUserListingMode() {
        Map<String, Serializable> props = getProperties();
        if (props.containsKey(USERS_LISTING_MODE_PROPERTY)) {
            return (String) props.get(USERS_LISTING_MODE_PROPERTY);
        }
        return SEARCH_ONLY_MODE;
    }

    protected String getFirstParameter() {
        Object[] parameters = getParameters();
        if (parameters.length > 0) {
            String param = (String) parameters[0];
            if (param != null) {
                return param.trim();
            }
        }
        return "";
    }

    protected List<DocumentModel> searchAllUsers(UserManager userManager) throws ClientException {
       return userManager.searchUsers(null);
    }

    protected List<DocumentModel> searchUsers(UserManager userManager) throws ClientException {
        List<DocumentModel> users = new ArrayList<DocumentModel>();
        String searchString = getFirstParameter();
        if ("*".equals(searchString)) {
            users = searchAllUsers(userManager);
        } else if (!StringUtils.isEmpty(searchString)) {
            users = userManager.searchUsers(searchString);
        }
        return users;
    }

    protected List<DocumentModel> searchUsersFromCatalog(UserManager userManager) throws ClientException {
        if (userCatalog == null) {
            updateUserCatalog(userManager);
        }
        String selectedLetter = getFirstParameter();
        if (StringUtils.isEmpty(selectedLetter)
                || !userCatalog.containsKey(selectedLetter)) {
            Collection<String> catalogLetters = getCatalogLetters();
            if (!catalogLetters.isEmpty()) {
                selectedLetter = catalogLetters.iterator().next();
            }
        }
        return userCatalog.get(selectedLetter);
    }

    protected void updateUserCatalog(UserManager userManager) throws ClientException {
        DocumentModelList allUsers = userManager.searchUsers(null);
        userCatalog = new HashMap<String, DocumentModelList>();
        String userSortField = userManager.getUserSortField();
        for (DocumentModel user : allUsers) {
            // FIXME: this should use a "display name" dedicated API
            String displayName = null;
            if (userSortField != null) {
                // XXX hack, principals have only one model
                org.nuxeo.ecm.core.api.DataModel dm = user.getDataModels().values().iterator().next();
                displayName = (String) dm.getData(userSortField);
            }
            if (StringUtils.isEmpty(displayName)) {
                displayName = user.getId();
            }
            String firstLetter = displayName.substring(0, 1).toUpperCase();
            DocumentModelList list = userCatalog.get(firstLetter);
            if (list == null) {
                list = new DocumentModelListImpl();
                userCatalog.put(firstLetter, list);
            }
            list.add(user);
        }
    }

    public Collection<String> getCatalogLetters() {
        if (userCatalog == null) {
            try {
                UserManager userManager = Framework.getService(UserManager.class);
                updateUserCatalog(userManager);
            } catch (Exception e) {
                log.error("Unable to update user catalog", e);
                return Collections.emptyList();
            }
        }
        List<String> list = new ArrayList<String>(userCatalog.keySet());
        Collections.sort(list);
        return list;
    }

    /**
     * This page provider does not support sort for now => override what may be
     * contributed in the definition
     */
    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    protected void pageChanged() {
        super.pageChanged();
        pageUsers = null;
    }

    @Override
    public void refresh() {
        super.refresh();
        pageUsers = null;
    }

}
