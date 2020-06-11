// Copyright 2020 Google LLC
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

import { simulateDonate, simulateSave } from '/js/donate-or-save.js'

const ANNUAL_DONATION = 5000;
const NUM_YEARS = 40; // The number of years in your career.

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(init);

/** Works like the range function in python. (https://www.geeksforgeeks.org/python-range-function/) */
function range(n) {
  return [...Array(n).keys()];
}

/** Make a table from an arbitrary amount of columns */
function makeRows() {
  let columns = Array.from(arguments);

  return range(columns[0].length).map(index => columns.map(col => col[index]));
}

function initTable(rows, columnHeaders) {
  let table = new google.visualization.DataTable();

  columnHeaders.forEach(header => table.addColumn('number', header));
  table.addRows(rows)

  return table;
}

function drawChart(table) {
  const options = {
    'title' : 'Donating vs Saving to Donate Later',
    'width' : 500,
    'height' : 400
  };

  const chart = new google.visualization.LineChart(
      document.getElementById('chart-container'));
  chart.draw(table, options);
}

function init() {
  let rows = makeRows(
                range(NUM_YEARS),
                simulateDonate(ANNUAL_DONATION, NUM_YEARS),
                simulateSave(ANNUAL_DONATION, NUM_YEARS)
              );

  let columnHeaders = ['Year', 'Donate right away', 'Save']

  let table = initTable(rows, columnHeaders);

  drawChart(table);
}
