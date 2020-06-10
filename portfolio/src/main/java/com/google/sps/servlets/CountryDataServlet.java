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

import com.google.sps.data.Country;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns country data as a JSON array */
@WebServlet("/country-data")
public class CountryDataServlet extends HttpServlet {

  private Collection<Country> countries;

  @Override
  public void init() {
    countries = new ArrayList<>();

    Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/country-data.csv"));

    // Eat first line that contains headings
    scanner.nextLine(); 

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      String country = parseCountry(cells[0]);
      String language = firstWord(cells[1]);
      
      countries.add(new Country(country, language));
    }
    scanner.close();
  }

  private String firstWord(String str) {
    int i = 0;

    while (i < str.length() && Character.isLetter(str.charAt(i))) {
      i++;
    }

    return str.substring(0, i);
  }
  private String parseCountry(String countryCell) {
    /** Parse the string that belongs to the country in the corresponing cell */
    
    // Some countries have footnotes at the end of their name (the data comes from Wikipedia)
    // The footnotes are in this form: Austria[10][11] 
    return countryCell.split("\\[")[0];
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(countries);
    response.getWriter().println(json);
  }
}
