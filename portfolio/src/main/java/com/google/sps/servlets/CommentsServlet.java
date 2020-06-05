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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/** Servlet that provides a list of comments */
@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PreparedQuery queryResult = runCommentsQuery();

    List<Comment> comments = toCommentList(queryResult);

    int numComments = Integer.parseInt(request.getParameter("num-comments"));
    comments = reduceSize(comments, numComments);

    String jsonComments = convertToJson(comments);
    
    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }

  private PreparedQuery runCommentsQuery() {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    return results;
  }

  private List<Comment> toCommentList(PreparedQuery queryResult) {
    List<Comment> comments = new ArrayList<>();

    for (Entity entity : queryResult.asIterable()) {
      long id = entity.getKey().getId();
      String content = (String) entity.getProperty("content");
      long timestamp = (long) entity.getProperty("timestamp");

      Comment comment = new Comment(id, content, timestamp);
      comments.add(comment); 
    }

    return comments;
  }

  /** Reduces the size of alist to maxSize if it is larger than maxSize*/
  private List<Comment> reduceSize(List<Comment> comments, int maxSize) {
    if (comments.size() > maxSize) {
      return comments.subList(0, maxSize);
    } else {
      return comments;
    }
  }

  /**
   * Converts a ServerStats instance into a JSON string using the Gson library. Note: We first added
   * the Gson library dependency to pom.xml.
   */
  private String convertToJson(List<Comment> comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }
}