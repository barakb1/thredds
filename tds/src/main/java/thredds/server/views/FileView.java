/*
 * Copyright 1998-2015 John Caron and University Corporation for Atmospheric Research/Unidata
 *
 *  Portions of this software were developed by the Unidata Program at the
 *  University Corporation for Atmospheric Research.
 *
 *  Access and use of this software shall impose the following obligations
 *  and understandings on the user. The user is granted the right, without
 *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  this software, and any derivative works thereof, and its supporting
 *  documentation for any purpose whatsoever, provided that this entire
 *  notice appears in all copies of the software, derivative works and
 *  supporting documentation.  Further, UCAR requests that the user credit
 *  UCAR/Unidata in any publications that result from the use of this
 *  software or in any product that includes this software. The names UCAR
 *  and/or Unidata, however, may not be used in any advertising or publicity
 *  to endorse or promote any products or commercial entity unless specific
 *  written permission is obtained from UCAR/Unidata. The user also
 *  understands that UCAR/Unidata is not obligated to provide the user with
 *  any support, consulting, training or assistance of any kind with regard
 *  to the use, operation and performance of this software nor to provide
 *  the user with any updates, revisions, new versions or "bug fixes."
 *
 *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package thredds.server.views;

import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.io.File;

import thredds.util.ContentType;
import ucar.nc2.util.IO;
import ucar.unidata.io.RandomAccessFile;

/**
 * LOOK: wants to replace ServletUtil.returnFile() ?
 * Associated with threddsFileView in tds/src/main/webapp/WEB-INF/view.xml
 *
 * Render the response to a request for a local file including byte range requests.
 * <p/>
 * <p/>
 * This view supports the following model elements:
 * <pre>
 *  KEY                  OBJECT             Required
 *  ===                  ======             ========
 * "file"               java.io.File         yes
 * "contentType"        java.lang.String     no
 * "characterEncoding"  java.lang.String     no
 * </pre>
 * <p/>
 * NOTE: If the content type is determined to be text, the character encoding
 * is assumed to be UTF-8 unless
 *
 * @author edavis
 * @since 4.0
 */
public class FileView extends AbstractView {

  protected void renderMergedOutputModel(Map model, HttpServletRequest req, HttpServletResponse res) throws Exception {
    if (model == null || model.isEmpty())
      throw new IllegalArgumentException("Model must not be null or empty.");
    if (!model.containsKey("file"))
      throw new IllegalArgumentException("Model must contain \"file\" key.");

    Object o = model.get("file");
    if (!(o instanceof File))
      throw new IllegalArgumentException("Object mapped by \"file\" key  must be a File.");
    File file = (File) o;

    // Check that file exists and is not a directory.
    if (!file.isFile()) {
      throw new IllegalArgumentException();
    }

    // Check if content type is specified.
    String contentType = null;
    if (model.containsKey("contentType")) {
      o = model.get("contentType");
      if (o instanceof String)
        contentType = (String) o;
    }

    // Check if characterEncoding is specified.
    String characterEncoding = null;
    if (model.containsKey("characterEncoding")) {
      o = model.get("characterEncoding");
      if (o instanceof String)
        characterEncoding = (String) o;
    }

    // Set the type of the file
    String filename = file.getPath();
    if (null == contentType) {
      ContentType type = ContentType.findContentTypeFromFilename(filename);

      if (type == null) {
        contentType = this.getServletContext().getMimeType(filename);  // let servlet have a shot at it
        if (null == contentType)
          type = ContentType.binary;                                   // nope use default
      }

      if (null == contentType)
        contentType = type.toString();
    }

    /* Do I need/want to do this?
    if (characterEncoding == null) {
      if ((!contentType.contains("charset=")) && (contentType.startsWith("text/") || contentType.startsWith("application/xml"))) {
        characterEncoding = "utf-8";
      }
    } */

    // Set content type and character encoding as given/determined.
    res.setContentType(contentType);
    if (characterEncoding != null)
      res.setCharacterEncoding(characterEncoding);

    // The rest of this is from John's thredds.servlet.ServletUtil.returnFile(...)
    // see if its a Range Request
    boolean isRangeRequest = false;
    long startPos = 0, endPos = Long.MAX_VALUE;
    String rangeRequest = req.getHeader("Range");
    if (rangeRequest != null) { // bytes=12-34 or bytes=12-
      int pos = rangeRequest.indexOf("=");
      if (pos > 0) {
        int pos2 = rangeRequest.indexOf("-");
        if (pos2 > 0) {
          String startString = rangeRequest.substring(pos + 1, pos2);
          String endString = rangeRequest.substring(pos2 + 1);
          startPos = Long.parseLong(startString);
          if (endString.length() > 0)
            endPos = Long.parseLong(endString) + 1;
          isRangeRequest = true;
        }
      }
    }

    // set content length
    long fileSize = file.length();
    long contentLength = fileSize;
    if (isRangeRequest) {
      endPos = Math.min(endPos, fileSize);
      contentLength = endPos - startPos;
    }
    res.setContentLength((int) contentLength);

    // indicate we allow Range Requests
    if (!isRangeRequest)
      res.addHeader("Accept-Ranges", "bytes");

    if (req.getMethod().equals("HEAD")) {
      return;
    }

    if (isRangeRequest) {
      // set before content is sent
      res.addHeader("Content-Range", "bytes " + startPos + "-" + (endPos - 1) + "/" + fileSize);
      res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

      try (RandomAccessFile craf = RandomAccessFile.acquire(filename)) {
        IO.copyRafB(craf, startPos, contentLength, res.getOutputStream(), new byte[60000]);
        return;
      }
    }

    // Return the file : let exceptions propagate and be caught
    ServletOutputStream out = res.getOutputStream();
    IO.copyFileB(file, out, 60000);
    res.flushBuffer();
    out.close();
  }
}