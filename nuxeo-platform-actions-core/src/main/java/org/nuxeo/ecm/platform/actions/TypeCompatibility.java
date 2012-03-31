/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     <a href=mailto:vpasquier@nuxeo.com>Vladimir Pasquier</a>
 */
package org.nuxeo.ecm.platform.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * UI type action compatibility descriptor from category action
 *
 */
@XObject("typeCompatibility")
public class TypeCompatibility {

    @XNode("@id")
    String id;

    @XNode("@type")
    String type;

    @XNodeList(value = "category", type = ArrayList.class, componentType = String.class)
    private List<String> categories = new ArrayList<String>();

    // WIP
    // @XNodeMap(value = "category", key = "@type", type = HashMap.class,
    // componentType = ActionTypeDescriptor.class)
    // Map<String, ActionTypeDescriptor> properties = new HashMap<String,
    // ActionTypeDescriptor>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

}
