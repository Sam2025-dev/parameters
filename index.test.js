'use strict';

const { test } = require('node:test');
const assert = require('node:assert/strict');
const pkg = require('./package.json');
const { name } = require('./index.js');

test('package name is parameters', () => {
  assert.equal(pkg.name, 'parameters');
});

test('exported name is parameters', () => {
  assert.equal(name, 'parameters');
});

test('repository no longer identifies as branch-kit', () => {
  assert.notEqual(pkg.name, 'branch-kit');
  assert.notEqual(name, 'branch-kit');
});
