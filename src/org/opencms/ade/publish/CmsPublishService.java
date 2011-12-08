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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * The implementation of the publish service.<p>
 * 
 * @since 8.0.0
 * 
 */
public class CmsPublishService extends CmsGwtService implements I_CmsPublishService {

    /** The publish project id parameter name. */
    public static final String PARAM_PUBLISH_PROJECT_ID = "publishProjectId";

    /** The version id for serialization. */
    private static final long serialVersionUID = 3852074177607037076L;

    /** Session attribute name constant. */
    private static final String SESSION_ATTR_ADE_PUB_OPTS_CACHE = "__OCMS_ADE_PUB_OPTS_CACHE__";

    /**
     * Returns a new publish service instance.<p>
     * 
     * @param request the servlet request
     * 
     * @return the service instance
     */
    public static CmsPublishService newInstance(HttpServletRequest request) {

        CmsPublishService srv = new CmsPublishService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        return srv;
    }

    /**
     * 
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#executeAction(java.util.List, java.util.List, String)
     */
    public CmsWorkflowResponse executeAction(List<CmsUUID> toPublish, List<CmsUUID> toRemove, String action)
    throws CmsRpcException {

        CmsWorkflowResponse response = null;
        try {
            CmsObject cms = getCmsObject();

            CmsPublish pub = new CmsPublish(cms, getCachedOptions());
            List<CmsResource> publishResources = idsToResources(cms, toPublish);
            pub.removeResourcesFromPublishList(toRemove);
            response = OpenCms.getWorkflowManager().executeAction(cms, action, publishResources);

        } catch (Throwable e) {
            error(e);
        }
        return response;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getInitData()
     */
    public CmsPublishData getInitData() throws CmsRpcException {

        CmsPublishData result = null;
        try {
            String projectParam = getRequest().getParameter(PARAM_PUBLISH_PROJECT_ID);
            CmsPublishOptions options = getCachedOptions();
            List<CmsProjectBean> projects = getProjects();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(projectParam) && CmsUUID.isValidUUID(projectParam)) {
                CmsUUID selectedProject = new CmsUUID(projectParam);
                // check if the selected project is a manageable project
                for (CmsProjectBean project : projects) {
                    if (selectedProject.equals(project.getId())) {
                        options.setProjectId(selectedProject);
                        break;
                    }
                }
            }
            result = new CmsPublishData(
                options,
                projects,
                getResourceGroups(options),
                OpenCms.getWorkflowManager().getAvailableActions(getCmsObject()));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getProjects()
     */
    public List<CmsProjectBean> getProjects() throws CmsRpcException {

        List<CmsProjectBean> result = null;
        try {
            result = new CmsPublish(getCmsObject()).getManageableProjects();
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getResourceGroups(org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public List<CmsPublishGroup> getResourceGroups(CmsPublishOptions options) throws CmsRpcException {

        List<CmsPublishGroup> results = null;
        try {
            CmsPublish pub = new CmsPublish(getCmsObject(), options);
            setCachedOptions(options);
            results = pub.getPublishGroups();
        } catch (Throwable e) {
            error(e);
        }
        return results;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getResourceOptions()
     */
    public CmsPublishOptions getResourceOptions() throws CmsRpcException {

        CmsPublishOptions result = null;
        try {
            result = getCachedOptions();
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * Returns the cached publish options, creating it if it doesn't already exist.<p>
     * 
     * @return the cached publish options
     */
    private CmsPublishOptions getCachedOptions() {

        CmsPublishOptions cache = (CmsPublishOptions)getRequest().getSession().getAttribute(
            SESSION_ATTR_ADE_PUB_OPTS_CACHE);
        if (cache == null) {
            cache = new CmsPublishOptions();
            getRequest().getSession().setAttribute(SESSION_ATTR_ADE_PUB_OPTS_CACHE, cache);
        }
        return cache;

    }

    /**
     * Converts a list of IDs to resources.<p>
     * 
     * @param cms the CmObject used for reading the resources 
     * @param ids the list of IDs
     * 
     * @return a list of resources 
     */
    private List<CmsResource> idsToResources(CmsObject cms, List<CmsUUID> ids) {

        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsUUID id : ids) {
            try {
                CmsResource resource = cms.readResource(id, CmsResourceFilter.ALL);
                result.add(resource);
            } catch (CmsException e) {
                // should never happen
                logError(e);
            }
        }
        return result;
    }

    /**
     * Saves the given options to the session.<p>
     * 
     * @param options the options to save
     */
    private void setCachedOptions(CmsPublishOptions options) {

        getRequest().getSession().setAttribute(SESSION_ATTR_ADE_PUB_OPTS_CACHE, options);
    }
}
