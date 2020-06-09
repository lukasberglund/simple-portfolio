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

const HONG_KONG_COORD = {lat: 22.300140, lng: 114.172237};

var geocoder = new google.maps.Geocoder;
var languageMap;

function setLocationLabel(location) {
  locationLabel = document.getElementById('location-label');

  if (location == "") {
    locationLabel.innerText = "Can't determine country";
  } else {
    locationLabel.innerText = location;
  }
}

function setLanguageLabel(location) {
  languageLabel = document.getElementById('language-label');

  if (location == "") {
    language = "Hard to tell";
  } else {
    language = languageMap[location];
  }

  languageLabel.innerText = 'Language spoken: ' + language;
}

function determineCountry(address) {
  countries = Object.keys(languageMap);

  for (i = 0; i < countries.length; i++) {
    country = countries[i];
    if (address.includes(country)) {
      return country;
    } 
  }

  console.log("Can't determine country for address \'" + address + "\'");
  return "";
}

function updateCountry(marker) {
  geocoder.geocode({'location': marker.getPosition()}, function(results, status) {
    var country;
    
    if (results[0] && results[0].formatted_address) {
      country = determineCountry(results[0].formatted_address);
    } else {
      country = "";
    }

    setLocationLabel(country);
    setLanguageLabel(country);
  });
}

function addMarker(map) {
  var marker = new google.maps.Marker({
    position: map.getCenter(),
    map: map,
    draggable:true,
  });
  updateCountry(marker);

  marker.addListener('dragend', function() {updateCountry(marker);});
}

function createMap() {

  const mapOptions = {
    center: HONG_KONG_COORD, 
    zoom: 6,
    mapTypeId: 'hybrid'
    }

  var map = new google.maps.Map(
      document.getElementById('map'),
      mapOptions);
  
  addMarker(map);
}

function toLanguageMap(arr) {
  /** Convert an array of country objects into a map from country to language */
  return arr.reduce((map, country) => {
    map[country.country] = country.language;
    return map;
  }, {});
}

function initLanguageMap() {
  return fetch("/country-data")
        .then(response => response.json())
        .then(arr => {
          languageMap = toLanguageMap(arr);
        });
  
}

function init() {
  initLanguageMap().then(createMap());
}