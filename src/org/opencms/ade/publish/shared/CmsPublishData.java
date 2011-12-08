/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.publish.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean that contains both publish options and a map of projects.<p>
 * 
 * @since 8.0
 */
public class CmsPublishData implements IsSerializable {

    /** Name of the used dictionary. */
    public static final String DICT_NAME = "org_opencms_ade_publish";

    /** The publish groups. */
    private List<CmsPublishGroup> m_groups;

    /** The publish options. */
    private CmsPublishOptions m_options;

    /** The list of projects. */
    private List<CmsProjectBean> m_projects;

    /** The available work flow actions. */
    private List<CmsWorkflowActionBean> m_workFlowActions;

    /** 
     * Creates a new instance.<p>
     * 
     * @param options the publish options 
     * @param projects the map of projects 
     * @param groups the publish groups
     * @param workFlowActions the available work flow actions
     */
    public CmsPublishData(
        CmsPublishOptions options,
        List<CmsProjectBean> projects,
        List<CmsPublishGroup> groups,
        List<CmsWorkflowActionBean> workFlowActions) {

        m_options = options;
        m_projects = projects;
        m_groups = groups;
        m_workFlowActions = workFlowActions;
    }

    /**
     * For serialization.<p>
     */
    protected CmsPublishData() {

        // for serialization
    }

    /**
     * Returns the publish groups.<p>
     *
     * @return the publish groups
     */
    public List<CmsPublishGroup> getGroups() {

        return m_groups;
    }

    /**
     * Returns the publish options.<p>
     * 
     * @return the publish options
     */
    public CmsPublishOptions getOptions() {

        return m_options;
    }

    /**
     * Returns the list of projects.<p>
     * 
     * @return the list of projects 
     */
    public List<CmsProjectBean> getProjects() {

        return m_projects;
    }

    /**
     * Returns the available work flow actions.<p>
     *
     * @return the available work flow actions
     */
    public List<CmsWorkflowActionBean> getWorkFlowActions() {

        return m_workFlowActions;
    }
}
