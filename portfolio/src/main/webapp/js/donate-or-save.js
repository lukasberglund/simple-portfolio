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

const GROWTH_RATE = 1.07;

/** Simulate the growth of monetary wealth (e.g a stock portfolio) */
export function addGrowth(startingValue, numCycles, data) {
  var value = startingValue;
  
  data.addRow([0, value]);

  for (let cycle = 1; cycle <= numCycles; cycle++) {
    value *= GROWTH_RATE;

    data.addRow([cycle, value]);
  }

  return data;
}

/** 
 *  Simulate how much money you would be donating over a career that lasts n years with 
 *  certain annual income, IF YOU INVESTED IT AND DONATED LATER.
 */
export function simulateSave(annualDonation, numYears) {
  let total = 0;
  let snapshots = new Array();

  for (let cycle = 0; cycle <= numYears; cycle++) {
    snapshots.push(total);
    total *= GROWTH_RATE;
    total += annualDonation;
    annualDonation *= GROWTH_RATE;
  }

  return snapshots;
}

/** 
 *  Simulate how much money you would be donating over a career that lasts n years with 
 *  certain annual income. We assume that your income grows by the same amount that a 
 *  stock portfolio grows over time.
 */
export function simulateDonate(annualDonation, numYears) {
  let totalDonated = 0;
  let snapshots = new Array();

  for (let cycle = 0; cycle <= numYears; cycle++) {
    snapshots.push(totalDonated);
    totalDonated += annualDonation;
    annualDonation *= GROWTH_RATE;
  }

  return snapshots;
}
