/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

let _uuid;
let rUndoable;
let getNode = (action) => {
  return {
    id: action.id || _uuid.v4(),
    name: action.name || 'no-name',
    endpoint: action.endpoint || 'LR',
    icon: action.icon || 'fa-plug',
    cssClass: action.cssClass || '',
    badgeInfo: action.badgeInfo || null,
    badgeCssClass: action.badgeCssClass || 'badge-info',
    badgeTooltip: action.badgeTooltip || null,
    tooltipCssClass: action.tooltipCssClass || '',
    disabled: action.disabled || false,
    selected: action.selected || false,
    nodeType: action.nodeType,
    _uiPosition: {
      left: '',
      top: ''
    }
  };
};
let nodes = (state = [], action = {}) => {
  switch(action.type) {
    case 'ADD-NODE':
      return [
        ...state,
        getNode(action.node)
      ];
    case 'REMOVE-NODE':
      return state.filter(node => node.id !== action.id);
    case 'RESET-SELECTED':
      return state.map( node => {
        node.selected = false;
        return node;
      });
    case 'UPDATE-NODE':
      let matchIndex;
      let matchNode = state.filter( (node, index) =>{
        if (node.id === action.id) {
          matchIndex = index;
          return true;
        }
      });
      if (matchNode && matchNode.length) {
        matchNode = matchNode[0];
        angular.extend(matchNode, action.config);
        return [
          ...state.slice(0, matchIndex),
          state[matchIndex],
          ...state.slice(matchIndex+1)
        ];
      } else {
        return state;
      }
      break;
    case 'SET-NODES':
      let nodes = action.nodes.map(getNode);
      return nodes;
    default:
      return state;
  }
};
let connections = (state = [], action={}) => {
  switch(action.type) {
    case 'SET-CONNECTIONS':
      return action.connections;
    default:
      return state;
  }
};
let isDagInitialized = (state = false, action={}) =>{
  switch(action.type) {
    case 'INIT-DAG':
      return true;
    case 'RESET-INIT-DAG':
      return false;
    default:
      return state;
  }
};
let isDisabled = (state = false, action = {}) => {
  switch(action.type) {
    case 'DISABLE-DAG':
      return true;
    case 'ENABLE-DAG':
      return false;
    default:
      return state;
  }
};

let Store = (Redux, uuid, Undoable) => {
  _uuid = uuid;
  rUndoable = Undoable;
  let combinedReducer = Redux.combineReducers({
    nodes: rUndoable.default(nodes),
    connections: rUndoable.default(connections),
    isDagInitialized: isDagInitialized,
    isDisabled: isDisabled
  });
  return Redux.createStore(combinedReducer);
};
Store.$inject = ['Redux', 'uuid', 'Undoable'];

angular.module(PKG.name + '.commons')
  .factory('MyDagStore', Store);
