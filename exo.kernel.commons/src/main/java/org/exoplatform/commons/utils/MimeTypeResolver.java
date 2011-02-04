/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.utils;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MimeTypeResolver
{
   private Map<String, List<String>> mimeTypes = new HashMap<String, List<String>>();

   private Map<String, List<String>> extentions = new HashMap<String, List<String>>();

   private String defaultMimeType = "application/octet-stream";

   public MimeTypeResolver()
   {
      Scanner scanner = null;
      try
      {
         SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<Void>()
         {
            public Void run() throws Exception
            {
               Scanner scanner = new Scanner(getClass().getResourceAsStream("mimetypes.properties"));
               while (scanner.hasNextLine())
               {
                  processLine(scanner.nextLine());
               }
               return null;
            }
         });
      }
      catch (IOException e)
      {
         throw new InternalError("Unable to load mimetypes: " + e.toString());
      }
      finally
      {
         if (scanner != null)
         {
            scanner.close();
         }
      }
   }

   /**
    * Returns default MIMEType.
    * 
    * @return String
    */
   public String getDefaultMimeType()
   {
      return defaultMimeType;
   }

   /**
    * Set default MIMEType.
    * 
    * @param defaultMimeType
    *          String, default MIMEType
    */
   public void setDefaultMimeType(String defaultMimeType)
   {
      this.defaultMimeType = defaultMimeType;
   }

   /**
    * Get MIMEType which corresponds to file extension. If file extension is unknown the default 
    * MIMEType will be returned. If there are more than one MIMETypes for specific extension the 
    * first occurred in the list will be returned. 
    * 
    * @param filename
    * @return String MIMEType
    */
   public String getMimeType(String filename)
   {
      String ext = filename.substring(filename.lastIndexOf(".") + 1);
      if (ext.equals(""))
      {
         ext = filename;
      }

      List<String> values = mimeTypes.get(ext);
      return values == null ? defaultMimeType : values.get(0);
   }

   /**
    * Get file extension corresponds to MIMEType. If MIMEType is empty or equals
    * default MIMEType empty string will be returned. If there is no file extension
    * for specific MIMEType the empty string will be returned also. In case when 
    * there are more than one extension for specific MIMEType the first occurred 
    * extension in the list will be returned if MIMEType ends with this extension
    * otherwise just first occurred.
    * 
    * @param mimeType
    *          MIMEType
    * @return file extension
    */
   public String getExtension(String mimeType)
   {
      if (mimeType.isEmpty() || mimeType.equals(defaultMimeType))
      {
         return "";
      }

      List<String> values = extentions.get(mimeType);
      if (values == null)
      {
         return "";
      }

      String resultExt = "";
      for (String ext : values)
      {
         if (mimeType.endsWith(ext))
         {
            return ext;
         }

         if (resultExt.isEmpty())
         {
            resultExt = new String(ext);
         }
      }
      return resultExt;
   }

   /**
    * Load MIMEType and corresponding extension.
    * 
    * @param aLine
    */
   protected void processLine(String aLine)
   {
      Scanner scanner = new Scanner(aLine);
      scanner.useDelimiter("=");
      while (scanner.hasNext())
      {
         String ext = scanner.next();
         String mimetype = scanner.next();

         // add mimetype
         List<String> values = mimeTypes.get(ext);
         if (values == null)
         {
            values = new ArrayList<String>();
            mimeTypes.put(ext, values);
         }
         values.add(mimetype);

         // add extension
         values = extentions.get(mimetype);
         if (values == null)
         {
            values = new ArrayList<String>();
            extentions.put(mimetype, values);
         }
         values.add(ext);
      }
   }
}
