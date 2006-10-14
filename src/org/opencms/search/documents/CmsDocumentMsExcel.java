/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/CmsDocumentMsExcel.java,v $
 * Date   : $Date: 2006/10/14 08:44:57 $
 * Version: $Revision: 1.9.8.2 $
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

package org.opencms.search.documents;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.extractors.CmsExtractorMsExcel;
import org.opencms.search.extractors.I_CmsExtractionResult;

import java.io.FileNotFoundException;

/**
 * Lucene document factory class to extract index data from a cms resource 
 * containing MS Excel data.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.9.8.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsDocumentMsExcel extends A_CmsVfsDocument {

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public CmsDocumentMsExcel(String name) {

        super(name);
    }

    /**
     * Returns the raw text content of a given vfs resource containing MS Excel data.<p>
     * 
     * @see org.opencms.search.documents.A_CmsVfsDocument#createDocument(CmsObject, A_CmsIndexResource, org.opencms.search.CmsSearchIndex)
     */
    public I_CmsExtractionResult extractContent(CmsObject cms, A_CmsIndexResource indexResource, CmsSearchIndex index)
    throws CmsIndexException, CmsException {

        CmsResource resource = (CmsResource)indexResource.getData();
        CmsFile file = readFile(cms, resource);

        try {
            return CmsExtractorMsExcel.getExtractor().extractText(file.getContents());
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                if ((e.getMessage() != null) && (e.getMessage().indexOf("Workbook") > 0)) {
                    // special case: catch Excel95 format error
                    throw new CmsIndexException(Messages.get().container(
                        Messages.ERR_NO_EXCEL_FORMAT_1,
                        resource.getRootPath()), e);
                }
            }
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }
}