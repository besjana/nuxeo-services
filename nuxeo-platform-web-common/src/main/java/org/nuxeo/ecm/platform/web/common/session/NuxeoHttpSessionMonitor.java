/*
 *
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.platform.web.common.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * Singleton used to keep track of all HttpSessions. This Singleton is
 * populated/updated either via the HttpSessionListener or via directedly via
 * the Authentication filter
 *
 * @author Tiry (tdelprat@nuxeo.com)
 * @since 5.4.2
 */
public class NuxeoHttpSessionMonitor {

    protected static NuxeoHttpSessionMonitor instance = new NuxeoHttpSessionMonitor();

    public static NuxeoHttpSessionMonitor instance() {
        return instance;
    }

    protected Map<String, SessionInfo> sessionTracker = new ConcurrentHashMap<String, SessionInfo>();

    public SessionInfo addEntry(HttpSession session) {
        if (session == null) {
            return null;
        }
        SessionInfo si = new SessionInfo(session.getId());
        sessionTracker.put(session.getId(), si);
        return si;
    }

    public SessionInfo associatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            SessionInfo si = sessionTracker.get(session.getId());
            if (si == null) {
                si = addEntry(session);
            }
            if (request.getUserPrincipal() != null) {
                si.setLoginName(request.getUserPrincipal().getName());
            }
            si.setLastAccessUrl(request.getRequestURI());
            return si;
        }
        return null;
    }

    public SessionInfo associatedUser(HttpSession session, String userName) {
        if (session == null) {
            return null;
        }
        SessionInfo si = sessionTracker.get(session.getId());
        if (si == null) {
            si = addEntry(session);
        }
        si.setLoginName(userName);
        return si;
    }

    public SessionInfo updateEntry(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            SessionInfo si = sessionTracker.get(session.getId());
            if (si != null) {
                si.updateLastAccessTime();
                si.setLastAccessUrl(request.getRequestURI());
                return si;
            } else {
                return addEntry(session);
            }
        }
        return null;
    }

    public void removeEntry(String sid) {
        sessionTracker.remove(sid);
    }

    public Collection<SessionInfo> getTrackedSessions() {
        return sessionTracker.values();
    }

    public List<SessionInfo> getSortedSessions() {

        List<SessionInfo> sortedSessions = new ArrayList<SessionInfo>();
        sortedSessions.addAll(getTrackedSessions());
        Collections.sort(sortedSessions);
        return sortedSessions;
    }

    public List<SessionInfo> getSortedSessions(long maxInactivity) {

        List<SessionInfo> sortedSessions = new ArrayList<SessionInfo>();
        for (SessionInfo si : getTrackedSessions()) {
            if (si.getInactivityInS() < maxInactivity) {
                sortedSessions.add(si);
            }
        }
        Collections.sort(sortedSessions);
        return sortedSessions;

    }

}