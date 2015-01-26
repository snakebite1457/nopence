/**
 * Created by Dennis Meyer on 20.01.2015.
 */

var nopence = namespace('Nopence');
var nopence_d3 = namespace('Nopence.D3');

function namespace(namespaceString) {
    var parts = namespaceString.split('.'),
        parent = window,
        currentPart = '';

    for(var i = 0, length = parts.length; i < length; i++) {
        currentPart = parts[i];
        parent[currentPart] = parent[currentPart] || {};
        parent = parent[currentPart];
    }

    return parent;
}