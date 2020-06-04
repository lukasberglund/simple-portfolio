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

const THOUGHTS = [ 
        'Most people are overconfident in regards to their beliefs',
        'If you own a blender you should make hummus at least once',
        'Fight Club, What\'s Eating Gilbert Grape and Uncut Gems are all great movies',
        'We should focus more on ensuring our species does not go extinct' ,
        'There is a reality that exists independently of our minds',
        'Strong physicalism with regards to the mind has a lot of issues',
        'While our intuition is good for spur of the moment decisions we shouldn\'t rely on it when making important life choices',
        'People can use whichever pronouns they want' ,
        'Punishment should be seen as a deterrent rather than a tool of justice',
        'At least some of these thoughts are wrong',
        '\"Follow your passion\" is bad advice'
        ];

/**
 * Given an integer n, picks a random number m such that 0 <= m < n 
 */
function randInt(n) {
    return Math.floor(Math.random() * n);
}

/**
 * Picks a random element from an array
 */
function chooseRandom(arr) {
    return arr[randInt(arr.length)];
}

function buildElement(type, content) {
  element = document.createElement(type, content);
  element.innerText = content;

  return element;
}

/**
 * Writes into the thought container 
 */
function writeToThoughtContainer(txt) {
    const thoughtContainer = document.getElementById('thought-container');
    thoughtContainer.innerText = txt;
}

/**
 * Adds a random thought to the page.
 */
function addRandomThought() {
  const thought = chooseRandom(THOUGHTS);

  writeToThoughtContainer(thought);
}

/** Creates a comment element containing text. */
function createCommentElement(comment) {
  const commentElement = document.createElement('div');
  
  commentElement.className = 'comment'
  
  commentElement.innerHTML = '';
  commentElement.appendChild(buildElement('p', comment.content));

  return commentElement;
}

function createInputField() {
  const field = document.createElement('input');

  field.type = 'text';
  field.name = 'new-comment';

  return field;
}

function createSubmitButton() {
  const button = document.createElement('input');

  button.type = 'submit';

  return button;
}

/** Creates form with which to add comments */
function createCommentForm() {
  const form = document.createElement('form');

  form.action = '/comments';
  form.method = 'POST'
  
  form.innerHTML = ''

  form.appendChild(buildElement('p', 'Post a comment'));
  form.appendChild(createInputField());
  form.appendChild(document.createElement('br'));
  form.appendChild(document.createElement('br'));  
  form.appendChild(createSubmitButton());

  return form;
}

/** Show a list of comment objects in the comment section */
function showComments(container, comments) {
  comments.forEach(comment => container.appendChild(createCommentElement(comment)));
}

function createCommentSection() {
  container = document.createElement('div');

  container.id = 'comment-section';
  container.innerHTML = '';

  fetch('/comments').then(response => response.json())
                    .then(comments => showComments(container, comments));

  return container;
}

/**Show comment section */
function showCommentSection() {
  container = document.getElementById('comment-container')
  
  container.innerHTML = '';
  container.appendChild(createCommentForm());
  container.appendChild(createCommentSection());
}
