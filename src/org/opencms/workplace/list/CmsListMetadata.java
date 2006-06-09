/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListMetadata.java,v $
 * Date   : $Date: 2006/06/09 15:16:15 $
 * Version: $Revision: 1.22.4.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.main.CmsIllegalStateException;
import org.opencms.util.CmsIdentifiableObjectContainer;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is class contains all the information for defining a whole html list.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.22.4.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListMetadata {

    /** Container for column definitions. */
    private CmsIdentifiableObjectContainer m_columns = new CmsIdentifiableObjectContainer(true, false);

    /** Container for of independent actions. */
    private CmsIdentifiableObjectContainer m_indepActions = new CmsIdentifiableObjectContainer(true, false);

    /** Container for item detail definitions. */
    private CmsIdentifiableObjectContainer m_itemDetails = new CmsIdentifiableObjectContainer(true, false);

    /** The id of the list. */
    private String m_listId;

    /** Container for multi actions. */
    private CmsIdentifiableObjectContainer m_multiActions = new CmsIdentifiableObjectContainer(true, false);

    /** Search action. */
    private CmsListSearchAction m_searchAction;

    /** if the data is self managed (sorted and filtered by {@link A_CmsListDialog#getListItems()} method). */
    private boolean m_selfManaged = false;

    /** if this metadata object should not be cached.<p>. */
    private boolean m_volatile = false;

    /** The related workplace dialog object. */
    private transient A_CmsListDialog m_wp;

    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the list 
     */
    public CmsListMetadata(String listId) {

        m_listId = listId;
    }

    /**
     * Adds a new column definition at the end.<p>
     * 
     * By default a column is printable if it is the first column in the list,
     * or if it is sorteable.<p>
     * 
     * If you want to override this behaviour, use the 
     * {@link CmsListColumnDefinition#setPrintable(boolean)}
     * method after calling this one.
     * 
     * @param listColumn the column definition
     * 
     * @see CmsIdentifiableObjectContainer
     */
    public void addColumn(CmsListColumnDefinition listColumn) {

        setListIdForColumn(listColumn);
        if (m_columns.elementList().isEmpty()) {
            listColumn.setPrintable(true);
        } else {
            listColumn.setPrintable(listColumn.isSorteable());
        }
        m_columns.addIdentifiableObject(listColumn.getId(), listColumn);
    }

    /**
     * Adds a new column definition at the given position.<p>
     * 
     * By default a column is printable if it is the first column in the list,
     * or if it is sorteable.<p>
     * 
     * If you want to override this behaviour, use the 
     * {@link CmsListColumnDefinition#setPrintable(boolean)}
     * method after calling this one.
     * 
     * @param listColumn the column definition
     * @param position the position
     * 
     * @see CmsIdentifiableObjectContainer
     */
    public void addColumn(CmsListColumnDefinition listColumn, int position) {

        setListIdForColumn(listColumn);
        if (m_columns.elementList().isEmpty()) {
            listColumn.setPrintable(true);
        } else {
            listColumn.setPrintable(listColumn.isSorteable());
        }
        m_columns.addIdentifiableObject(listColumn.getId(), listColumn, position);
    }

    /**
     * Adds a list item independent action.<p>
     * 
     * @param action the action
     */
    public void addIndependentAction(I_CmsListAction action) {

        action.setListId(getListId());
        m_indepActions.addIdentifiableObject(action.getId(), action);
    }

    /**
     * Adds a new item detail definition at the end.<p>
     * 
     * @param itemDetail the item detail definition
     * 
     * @see CmsIdentifiableObjectContainer
     */
    public void addItemDetails(CmsListItemDetails itemDetail) {

        itemDetail.setListId(getListId());
        m_itemDetails.addIdentifiableObject(itemDetail.getId(), itemDetail);
    }

    /**
     * Adds a new item detail definition at the given position.<p>
     * 
     * @param itemDetail the item detail definition
     * @param position the position
     * 
     * @see CmsIdentifiableObjectContainer
     */
    public void addItemDetails(CmsListItemDetails itemDetail, int position) {

        itemDetail.setListId(getListId());
        m_itemDetails.addIdentifiableObject(itemDetail.getId(), itemDetail, position);
    }

    /**
     * Adds an action applicable to more than one list item at once.<p>
     * 
     * It will be executed with a list of <code>{@link CmsListItem}</code>s.<p> 
     *  
     * @param multiAction the action
     */
    public void addMultiAction(CmsListMultiAction multiAction) {

        multiAction.setListId(getListId());
        m_multiActions.addIdentifiableObject(multiAction.getId(), multiAction);
    }

    /**
     * Generates the csv output for an empty table.<p>
     * 
     * @return csv output
     */
    public String csvEmptyList() {

        StringBuffer html = new StringBuffer(512);
        html.append("\n");
        return html.toString();
    }

    /**
     * Returns the csv output for the header of the list.<p>
     * 
     * @return csv output
     */
    public String csvHeader() {

        StringBuffer csv = new StringBuffer(1024);
        Iterator itCols = m_columns.elementList().iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            if (!col.isVisible()) {
                continue;
            }
            csv.append(col.csvHeader());
            csv.append("\t");
        }
        csv.append("\n\n");
        return csv.toString();
    }

    /**
     * Returns the csv output for a list item.<p>
     * 
     * @param item the list item to render
     * 
     * @return csv output
     */
    public String csvItem(CmsListItem item) {

        StringBuffer csv = new StringBuffer(1024);
        Iterator itCols = m_columns.elementList().iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            if (!col.isVisible()) {
                continue;
            }
            csv.append(col.csvCell(item));
            csv.append("\t");
        }
        csv.append("\n");
        return csv.toString();
    }

    /**
     * Returns a column definition object for a given column id.<p>
     * 
     * @param columnId the column id
     * 
     * @return the column definition, or <code>null</code> if not present
     */
    public CmsListColumnDefinition getColumnDefinition(String columnId) {

        return (CmsListColumnDefinition)m_columns.getObject(columnId);
    }

    /**
     * Returns all columns definitions.<p>
     * 
     * @return a list of <code>{@link CmsListColumnDefinition}</code>s.
     */
    public List getColumnDefinitions() {

        return m_columns.elementList();
    }

    /**
     * Returns an independent action object for a given id.<p>
     * 
     * @param actionId the id
     * 
     * @return the independent action, or <code>null</code> if not present
     */
    public I_CmsListAction getIndependentAction(String actionId) {

        return (I_CmsListAction)m_indepActions.getObject(actionId);
    }

    /**
     * Returns the list of independent actions.<p>
     * 
     * @return a list of <code>{@link I_CmsListAction}</code>s
     */
    public List getIndependentActions() {

        return m_indepActions.elementList();
    }

    /**
     * Returns the item details definition object for a given id.<p>
     * 
     * @param itemDetailId the id
     * 
     * @return the item details definition, or <code>null</code> if not present
     */
    public CmsListItemDetails getItemDetailDefinition(String itemDetailId) {

        return (CmsListItemDetails)m_itemDetails.getObject(itemDetailId);
    }

    /**
     * Returns all detail definitions.<p>
     * 
     * @return a list of <code>{@link CmsListItemDetails}</code>.
     */
    public List getItemDetailDefinitions() {

        return m_itemDetails.elementList();
    }

    /**
     * Returns the id of the list.<p>
     *
     * @return the id of list
     */
    public String getListId() {

        return m_listId;
    }

    /**
     * Returns a multi action object for a given id.<p>
     * 
     * @param actionId the id
     * 
     * @return the multi action, or <code>null</code> if not present
     */
    public CmsListMultiAction getMultiAction(String actionId) {

        return (CmsListMultiAction)m_multiActions.getObject(actionId);
    }

    /**
     * Returns the list of multi actions.<p>
     * 
     * @return a list of <code>{@link CmsListMultiAction}</code>s
     */
    public List getMultiActions() {

        return m_multiActions.elementList();
    }

    /**
     * Returns the search action.<p>
     *
     * @return the search action
     */
    public CmsListSearchAction getSearchAction() {

        return m_searchAction;
    }

    /**
     * Returns the total number of displayed columns.<p>
     * 
     * @return the total number of displayed columns
     */
    public int getWidth() {

        return m_columns.elementList().size() + (hasCheckMultiActions() ? 1 : 0);
    }

    /**
     * Returns the related workplace dialog.<p>
     *
     * @return the related workplace dialog
     */
    public A_CmsListDialog getWp() {

        return m_wp;
    }

    /**
     * Returns <code>true</code> if the list definition contains an action.<p>
     * 
     * @return <code>true</code> if the list definition contains an action
     */
    public boolean hasActions() {

        return !m_indepActions.elementList().isEmpty();
    }

    /**
     * Returns <code>true</code> if at least 'check' multiaction has been set.<p>
     * 
     * @return <code>true</code> if at least 'check' multiaction has been set
     */
    public boolean hasCheckMultiActions() {

        Iterator it = m_multiActions.elementList().iterator();
        while (it.hasNext()) {
            CmsListMultiAction action = (CmsListMultiAction)it.next();
            if (!(action instanceof CmsListRadioMultiAction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the list definition contains a multi action.<p>
     * 
     * @return <code>true</code> if the list definition contains a multi action
     */
    public boolean hasMultiActions() {

        return !m_multiActions.elementList().isEmpty();
    }

    /**
     * Returns <code>true</code> if any column definition contains a single action.<p>
     * 
     * @return <code>true</code> if any column definition contains a single action
     */
    public boolean hasSingleActions() {

        Iterator itCols = m_columns.elementList().iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            if (!col.getDefaultActions().isEmpty() || !col.getDirectActions().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the html code for the action bar.<p>
     * 
     * @return html code
     */
    public String htmlActionBar() {

        StringBuffer html = new StringBuffer(1024);
        html.append("<td class='misc'>\n");
        html.append("\t<div>\n");
        Iterator itDetails = m_itemDetails.elementList().iterator();
        while (itDetails.hasNext()) {
            I_CmsListAction detailAction = ((CmsListItemDetails)itDetails.next()).getAction();
            html.append("\t\t");
            html.append(detailAction.buttonHtml());
            if (itDetails.hasNext()) {
                html.append("&nbsp;&nbsp;");
            }
            html.append("\n");
        }
        Iterator itActions = m_indepActions.elementList().iterator();
        while (itActions.hasNext()) {
            I_CmsListAction indepAction = (I_CmsListAction)itActions.next();
            html.append("\t\t");
            html.append("&nbsp;&nbsp;");
            html.append(indepAction.buttonHtml());
            html.append("\n");
        }
        html.append("\t</div>\n");
        html.append("</td>\n");
        return html.toString();
    }

    /**
     * Generates the hml code for an empty table.<p>
     * 
     * @return html code
     */
    public String htmlEmptyTable() {

        StringBuffer html = new StringBuffer(512);
        html.append("<tr class='oddrowbg'>\n");
        html.append("\t<td align='center' colspan='");
        html.append(getWidth());
        html.append("'>\n");
        html.append(Messages.get().getBundle(getWp().getLocale()).key(Messages.GUI_LIST_EMPTY_0));
        html.append("\t</td>\n");
        html.append("</tr>\n");
        return html.toString();
    }

    /**
     * Returns the html code for the header of the list.<p>
     * 
     * @param list the list to generate the code for
     * 
     * @return html code
     */
    public String htmlHeader(CmsHtmlList list) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<tr>\n");
        Iterator itCols = m_columns.elementList().iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            if (!list.isPrintable() || col.isPrintable()) {
                html.append(col.htmlHeader(list));
            }
        }
        if (!list.isPrintable() && hasCheckMultiActions()) {
            html.append("\t<th width='0' class='select'>\n");
            html.append("\t\t<input type='checkbox' class='checkbox' name='listSelectAll' value='true' onClick=\"listSelect('");
            html.append(list.getId());
            html.append("')\">\n");
            html.append("\t</th>\n");
        }
        html.append("</tr>\n");
        return html.toString();
    }

    /**
     * Returns the html code for a list item.<p>
     * 
     * @param item the list item to render
     * @param odd if the position is odd or even
     * @param isPrintable if the list is to be printed
     * 
     * @return html code
     */
    public String htmlItem(CmsListItem item, boolean odd, boolean isPrintable) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<tr ");
        if (!isPrintable) {
            html.append("class='");
            html.append(odd ? "oddrowbg" : "evenrowbg");
            html.append("'");
        }
        html.append(">\n");
        Iterator itCols = m_columns.elementList().iterator();
        int width = 0;
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            if (!col.isVisible() || (isPrintable && !col.isPrintable())) {
                continue;
            }
            width++;
            StringBuffer style = new StringBuffer(64);
            html.append("<td");
            CmsListColumnAlignEnum align = col.getAlign();
            if (align != CmsListColumnAlignEnum.ALIGN_LEFT && CmsStringUtil.isNotEmpty(align.toString())) {
                style.append("text-align: ");
                style.append(col.getAlign());
                style.append("; ");
            }
            if (col.isTextWrapping()) {
                style.append("white-space: normal;");
            }
            if (isPrintable) {
                style.append("border-top: 1px solid black;");
            }
            if (style.length() > 0) {
                html.append(" style='");
                html.append(style);
                html.append("'");
            }
            html.append(">\n");
            html.append(col.htmlCell(item, isPrintable));
            html.append("</td>\n");
        }
        if (!isPrintable && hasCheckMultiActions()) {
            width++;
            html.append("\t<td class='select' align='center'>\n");
            html.append("\t\t<input type='checkbox' class='checkbox' name='listMultiAction' value='");
            html.append(item.getId());
            html.append("'>\n");
            html.append("\t</td>\n");
        }
        html.append("</tr>\n");

        Iterator itDet = m_itemDetails.elementList().iterator();
        while (itDet.hasNext()) {
            CmsListItemDetails lid = (CmsListItemDetails)itDet.next();
            if (lid.isVisible()
                && item.get(lid.getId()) != null
                && CmsStringUtil.isNotEmptyOrWhitespaceOnly(item.get(lid.getId()).toString())) {
                int padCols = 0;
                itCols = m_columns.elementList().iterator();
                while (itCols.hasNext()) {
                    CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
                    if (!col.isVisible() || (isPrintable && !col.isPrintable())) {
                        continue;
                    }
                    if (col.getId().equals(lid.getAtColumn())) {
                        break;
                    }
                    padCols++;
                }
                int spanCols = width - padCols;

                html.append("<tr ");
                if (!isPrintable) {
                    html.append("class='");
                    html.append(odd ? "oddrowbg" : "evenrowbg");
                    html.append("'");
                }
                html.append(">\n");
                if (padCols > 0) {
                    html.append("<td colspan='");
                    html.append(padCols);
                    html.append("'>&nbsp;</td>\n");
                }
                html.append("<td colspan='");
                html.append(spanCols);
                html.append("' style='padding-left: 20px; white-space:normal;'>\n");
                html.append(lid.htmlCell(item, isPrintable));
                html.append("\n</td>\n");
                html.append("\n");
                html.append("</tr>\n");
            }
        }
        return html.toString();
    }

    /**
     * Returns the html code for the multi action bar.<p>
     * 
     * @return html code
     */
    public String htmlMultiActionBar() {

        StringBuffer html = new StringBuffer(1024);
        html.append("<td class='misc'>\n");
        html.append("\t<div>\n");
        Iterator itActions = m_multiActions.elementList().iterator();
        while (itActions.hasNext()) {
            CmsListMultiAction multiAction = (CmsListMultiAction)itActions.next();
            html.append("\t\t");
            html.append(multiAction.buttonHtml());
            if (itActions.hasNext()) {
                html.append("&nbsp;&nbsp;");
            }
            html.append("\n");
        }
        html.append("\t</div>\n");
        html.append("</td>\n");
        return html.toString();
    }

    /**
     * Generates the html code for the search bar.<p>
     * 
     * @return html code
     */
    public String htmlSearchBar() {

        if (!isSearchable()) {
            return "";
        }
        StringBuffer html = new StringBuffer(1024);
        html.append("<td class='main'>\n");
        html.append("\t<div>\n");
        html.append("\t\t<input type='text' name='listSearchFilter' value='' size='20' maxlength='245' style='vertical-align: bottom;'>\n");
        html.append(m_searchAction.buttonHtml());
        I_CmsListAction showAllAction = m_searchAction.getShowAllAction();
        if (showAllAction != null) {
            html.append("&nbsp;&nbsp;");
            html.append(showAllAction.buttonHtml());
        }
        html.append("\t</div>\n");
        html.append("</td>\n");
        return html.toString();
    }

    /**
     * Returns <code>true</code> if the list is searchable.<p>
     * 
     * @return  <code>true</code> if the list is searchable
     */
    public boolean isSearchable() {

        return m_searchAction != null;
    }

    /**
     * Returns the self Managed flag.<p>
     *
     * @return the self Managed flag
     */
    public boolean isSelfManaged() {

        return m_selfManaged;
    }

    /**
     * Returns <code>true</code> if any column is sorteable.<p>
     * 
     * @return <code>true</code> if any column is sorteable
     */
    public boolean isSorteable() {

        Iterator itCols = m_columns.elementList().iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itCols.next();
            if (col.isSorteable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if this metadata object should not be cached.<p>
     * 
     * @return <code>true</code> if this metadata object should not be cached.<p>
     */
    public boolean isVolatile() {

        return m_volatile;
    }

    /**
     * Sets the search action.<p>
     *
     * @param searchAction the search action to set
     */
    public void setSearchAction(CmsListSearchAction searchAction) {

        m_searchAction = searchAction;
        if (m_searchAction != null) {
            m_searchAction.setListId(getListId());
        }
    }

    /**
     * Sets the self Managed flag.<p>
     *
     * @param selfManaged the self Managed flag to set
     */
    public void setSelfManaged(boolean selfManaged) {

        this.m_selfManaged = selfManaged;
    }

    /**
     * Sets the volatile flag.<p>
     *
     * @param volatileFlag the volatile flag to set
     */
    public void setVolatile(boolean volatileFlag) {

        m_volatile = volatileFlag;
    }

    /**
     * Sets the related workplace dialog.<p>
     *
     * @param wp the related workplace dialog to set
     */
    public void setWp(A_CmsListDialog wp) {

        m_wp = wp;
        Iterator itCols = getColumnDefinitions().iterator();
        while (itCols.hasNext()) {
            CmsListColumnDefinition column = (CmsListColumnDefinition)itCols.next();
            column.setWp(wp);
        }
        Iterator itDets = getItemDetailDefinitions().iterator();
        while (itDets.hasNext()) {
            CmsListItemDetails detail = (CmsListItemDetails)itDets.next();
            detail.setWp(wp);
        }
        Iterator itMultiActs = getMultiActions().iterator();
        while (itMultiActs.hasNext()) {
            CmsListMultiAction action = (CmsListMultiAction)itMultiActs.next();
            action.setWp(wp);
        }
        Iterator itIndActs = getIndependentActions().iterator();
        while (itIndActs.hasNext()) {
            I_CmsListAction action = (I_CmsListAction)itIndActs.next();
            action.setWp(wp);
        }
        if (m_searchAction != null) {
            m_searchAction.setWp(wp);
        }
    }

    /**
     * Toggles the given item detail state from visible to hidden or
     * from hidden to visible.<p>
     * 
     * @param itemDetailId the item detail id
     */
    public void toogleDetailState(String itemDetailId) {

        CmsListItemDetails lid = (CmsListItemDetails)m_itemDetails.getObject(itemDetailId);
        lid.setVisible(!lid.isVisible());
    }

    /**
     * Throws a runtime exception if there are 2 identical ids.<p> 
     * 
     * This includes:<p>
     * <ul>
     *      <li><code>{@link CmsListIndependentAction}</code>s</li>
     *      <li><code>{@link CmsListMultiAction}</code>s</li>
     *      <li><code>{@link CmsListItemDetails}</code></li>
     *      <li><code>{@link CmsListColumnDefinition}</code>s</li>
     *      <li><code>{@link CmsListDefaultAction}</code>s</li>
     *      <li><code>{@link CmsListDirectAction}</code>s</li>
     * </ul>
     */
    /*package*/void checkIds() {

        Set ids = new TreeSet();
        // indep actions
        Iterator itIndepActions = getIndependentActions().iterator();
        while (itIndepActions.hasNext()) {
            String id = ((CmsListIndependentAction)itIndepActions.next()).getId();
            if (ids.contains(id)) {
                throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_DUPLICATED_ID_1, id));
            }
            ids.add(id);
        }
        // multi actions
        Iterator itMultiActions = getMultiActions().iterator();
        while (itMultiActions.hasNext()) {
            String id = ((CmsListMultiAction)itMultiActions.next()).getId();
            if (ids.contains(id)) {
                throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_DUPLICATED_ID_1, id));
            }
            ids.add(id);
        }
        // details
        Iterator itItemDetails = getItemDetailDefinitions().iterator();
        while (itItemDetails.hasNext()) {
            String id = ((CmsListItemDetails)itItemDetails.next()).getId();
            if (ids.contains(id)) {
                throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_DUPLICATED_ID_1, id));
            }
            ids.add(id);
        }
        // columns
        Iterator itColumns = getColumnDefinitions().iterator();
        while (itColumns.hasNext()) {
            CmsListColumnDefinition col = (CmsListColumnDefinition)itColumns.next();
            if (ids.contains(col.getId())) {
                throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_DUPLICATED_ID_1, col.getId()));
            }
            ids.add(col.getId());
            // default actions
            Iterator itDefaultActions = col.getDefaultActions().iterator();
            while (itDefaultActions.hasNext()) {
                CmsListDefaultAction action = (CmsListDefaultAction)itDefaultActions.next();
                if (ids.contains(action.getId())) {
                    throw new CmsIllegalStateException(Messages.get().container(
                        Messages.ERR_DUPLICATED_ID_1,
                        action.getId()));
                }
                ids.add(action.getId());
            }
            // direct actions
            Iterator itDirectActions = col.getDirectActions().iterator();
            while (itDirectActions.hasNext()) {
                CmsListDirectAction action = (CmsListDirectAction)itDirectActions.next();
                if (ids.contains(action.getId())) {
                    throw new CmsIllegalStateException(Messages.get().container(
                        Messages.ERR_DUPLICATED_ID_1,
                        action.getId()));
                }
                ids.add(action.getId());
            }
        }
    }

    /**
     * Sets the list id for all column single actions.<p>
     * 
     * @param col the column to set the list id for
     */
    private void setListIdForColumn(CmsListColumnDefinition col) {

        col.setListId(getListId());
        // default actions
        Iterator itDefaultActions = col.getDefaultActions().iterator();
        while (itDefaultActions.hasNext()) {
            ((CmsListDefaultAction)itDefaultActions.next()).setListId(getListId());
        }
        // direct actions
        Iterator itDirectActions = col.getDirectActions().iterator();
        while (itDirectActions.hasNext()) {
            ((CmsListDirectAction)itDirectActions.next()).setListId(getListId());
        }
    }
}