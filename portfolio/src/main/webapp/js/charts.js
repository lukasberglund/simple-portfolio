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

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

const GROWTH_RATE = 1.07;

/** Simulate the growth of monetary wealth (e.g a stock portfolio) */
function addGrowth(startingValue, numCycles, data) {
  var value = startingValue;
  
  data.addRow([0, value]);

  for (cycle = 1; cycle <= numCycles; cycle++) {
    value *= GROWTH_RATE;

    data.addRow([cycle, value]);
  }

  return data;
}

function initTable() {
  var data = new google.visualization.DataTable();
  data.addColumn('number', 'Value');
  data.addColumn('number', 'Year');

  data = addGrowth(10000, 40, data);

  return data;
}

function drawChart() {
  const data = initTable();
  console.log('hey');
  
  const options = {
    'title' : 'Growth',
    'width' : 500,
    'height' : 400
  };

  console.log(data);

  const chart = new google.visualization.LineChart(
      document.getElementById('chart-container'));
  chart.draw(data, options);
}
