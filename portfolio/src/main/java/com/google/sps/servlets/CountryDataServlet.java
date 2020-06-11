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
      String language = listNaturally(parseLanguages(cells[1]));
      
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

  /** Parse the string that belongs to the country in the corresponing cell */
  private String parseCountry(String countryCell) {
    
    // Some countries have footnotes at the end of their name (the data comes from Wikipedia)
    // The footnotes are in this form: Austria[10][11] 
    return countryCell.split("\\[")[0];
  }

  private String[] parseLanguages(String languageCell) {
    // Sometimes there is information included in parentheses about the regions in which the languages are spoken.
    // We remove that information.
    return removeParens(languageCell).split(" ");
  }

  /* Remove parentheses along with contents from a string. E.g removeParens("My name is (this will get removed) george") => "My name is george" */
  private String removeParens(String str, char opener, char closer) {
    int openIndex = str.indexOf(opener);

    if (openIndex == -1) {
      // No opening parenthesis found.
      return str;
    } else {
      int closeIndex = str.indexOf(closer);
      
      String newStr;
      if (closeIndex == -1) {
        newStr = str.substring(0, openIndex);
      } else {
        newStr = str.substring(0, openIndex) + str.substring(closeIndex + 1);
      }

      // Remove remaining parentheses and return.
      return removeParens(newStr);
    }
  }

  /* List an array of string naturally in a string. E.g listNaturally("one", "two", "three") => "one, two and three" */
  private String listNaturally(String[] arr) {
    if (arr.length == 0) {
      return "";
    } else if (arr.length == 1) {
      return arr[0];
    } else {
      String acc = arr[0];
      int i = 1;

      while (i < arr.length - 1) {
        acc += ", " + arr[i];
        i++;
      }

      acc += " and " + arr[i];

      return acc;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(countries);
    response.getWriter().println(json);
  }
}
