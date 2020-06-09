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

function last_elem(arr) {
  return arr[arr.length - 1];
}

function updateCountry(marker) {
  var geocoder = new google.maps.Geocoder;

  return geocoder.geocode({'location': marker.getPosition()}, function(results, status) {
    const addressComponents = results[0].address_components;
    const mostGeneralComponent = last_elem(addressComponents);
    const country = mostGeneralComponent.long_name;

    marker.setLabel(country);
  });
}

function addMarker(map) {
  var marker = new google.maps.Marker({
    position: map.getCenter(),
    map: map,
    title: 'Hello World!',
  });
  updateCountry(marker);
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
