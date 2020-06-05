// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that manages indidual comments*/
@WebServlet("/comment/*")
public class CommentServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Entity commentEntity = buildCommentEntity(request);

    storeEntity(commentEntity);

    response.sendRedirect("/index.html");
  }

  private Entity buildCommentEntity (HttpServletRequest request) {
    Entity commentEntity = new Entity("Comment");

    commentEntity.setProperty("content", request.getParameter("new-comment"));
    commentEntity.setProperty("timestamp", System.currentTimeMillis());

    return commentEntity;
  }

  private void storeEntity(Entity entity) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entity);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long commentId = getCommentId(request);
    deleteComment(commentId);
  }
  
  /** If a comment with the id 42 is deleted the request will be to the path 'comment/42'
    *  getPathInfo() retrieves everything after 'comment', so in this case we would get '/42'.
    *  We then split the string on '/' into up to three pieces, in this case it returns ["", "42"]
    *  We then take the string at index 1 and convert it to a long.
    */
  private long getCommentId(HttpServletRequest request) throws IllegalArgumentException {
    
    String pathString = request.getPathInfo();
    String[] sections = pathString.split("/", 3);
    
    if (sections.length < 2) {
      throw new IllegalArgumentException("Request path '" + pathString + "' too short. Path must contain the ID of the comment to be deleted");
    } else {
      String idStr = sections[1];
      long id = Long.valueOf(idStr);

      return id;
    }
  }

  private void deleteComment(long id) {
    Key key = KeyFactory.createKey("Comment", id);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.delete(key);
  }
}
