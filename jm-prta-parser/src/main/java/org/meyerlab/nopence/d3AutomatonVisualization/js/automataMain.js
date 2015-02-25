/**
 * Created by Dennis Meyer on 20.02.2015.
 */

var org_meyerlab_nopence = org_meyerlab_nopence || {};

(function() {

    var helper = function() {

        // Privates
        this.transformToAssocArray = function(prmstr) {
            var params = {};
            var prmarr = prmstr.split("&");
            for ( var i = 0; i < prmarr.length; i++) {
                var tmparr = prmarr[i].split("=");
                params[tmparr[0]] = tmparr[1];
            }
            return params;
        }
    };

    // Public
    helper.prototype.GetParams = function() {
        var prmstr = window.location.search.substr(1);
        return prmstr != null && prmstr != "" ? this.transformToAssocArray(prmstr) : {};
    };

    helper.prototype.Tooltip = function(accessor){
        return function(selection){
            var tooltipDiv;
            var bodyNode = d3.select('body').node();
            selection.on("mouseover", function(d, i){
                // Clean up lost tooltips
                d3.select('body').selectAll('div.D3tooltip').remove();
                // Append tooltip
                tooltipDiv = d3.select('body').append('div').attr('class', 'D3tooltip');
                var absoluteMousePos = d3.mouse(bodyNode);
                tooltipDiv.style('left', (absoluteMousePos[0] + 10)+'px')
                    .style('top', (absoluteMousePos[1] - 15)+'px')
                    .style('position', 'absolute')
                    .style('z-index', 1001);
                // Add text using the accessor function
                var tooltipText = accessor(d, i) || '';
                // Crop text arbitrarily
                tooltipDiv.style('width', function(d, i){return (tooltipText.length > 80) ? '400px' : null;})
                    .html(tooltipText);
            })
                .on('mousemove', function(d, i) {
                    // Move tooltip
                    var absoluteMousePos = d3.mouse(bodyNode);
                    tooltipDiv.style('left', (absoluteMousePos[0] + 10)+'px')
                        .style('top', (absoluteMousePos[1] - 15)+'px');
                    var tooltipText = accessor(d, i) || '';
                    tooltipDiv.html(tooltipText);
                })
                .on("mouseout", function(d, i){
                    // Remove tooltip
                    tooltipDiv.remove();
                });

        };
    };

    helper.prototype.FillMarkAttributes = function(attributes) {
        var uniqueAttr = [];
        var uniqueAttrNames = [];

        attributes.forEach(function (a) {
            if (uniqueAttr.indexOf(a.OriginalId) === -1) {
                uniqueAttr.push(a.OriginalId);

                var attrName = a.Name.split(";")[0];
                uniqueAttrNames.push({
                    name: attrName,
                    originalId: a.OriginalId
                });
            }
        });

        uniqueAttrNames.forEach(function (a) {
            $("#dropMarkAttributes")
                .append("<li><input type='radio' id=" +  a.originalId   + " name='NAME' value="+ a.originalId +"><label for="+  a.originalId   + ">" + a.name + "</label></li>");
        });

        // Enable button
        $("#btnMarkAttributes").prop('disabled', false);

        // Register listener
        $("#dropMarkAttributes").on('click', 'li input', function(){
            org_meyerlab_nopence.d3Visual.markAttr(parseInt($(this).val()));
        });
    };

    helper.prototype.RetrieveAutomatonFileNames = function() {
        var data = {
            "action": "aFiles"
        };
        $.ajax({
            type: "POST",
            dataType: "json",
            url: "../php/service.php", //Relative or absolute path to response.php file
            data: data,
            success: function(fileNames) {
                fileNames.forEach(function (f) {
                    $("#dropAutomatonFile")
                        .append("<li><input type='radio' id=" + f.id + " name='NAME' value="+ f.filePath +"><label for="+ f.id + ">" + f.fileName + "</label></li>");
                });
            }
        });

        // Register listener
        $("#dropAutomatonFile").on('click', 'li input', function(){
            org_meyerlab_nopence.d3Visual.ShowAutomata($(this).val())
        });
    };

    // Export
    org_meyerlab_nopence.helper = new helper();
})();

(function() {

    var d3Visual = function () {

        var self = this;

        $.ajaxSetup({ async: false });
        $('#sliderProb').slider("setValue", 0);
        $('#sliderProb').on("slideStop", function(ev) {
            if (ev.value === 0) {
                //self.edges = $.extend(true, [], self.originalEdges);
            }
            else {
                self.nodes.forEach(function (n) {
                    var outEdges = self.originalEdges.filter(function (e) {
                        return n.Id === e.source.Id;
                    });

                    var sumWeight = 0;
                    outEdges.forEach(function (e) {
                        sumWeight += e.weight;
                    });

                    outEdges.forEach(function (e) {
                        var probability = e.weight / sumWeight;

                        var curNode = self.edges.filter(function (se) {
                           return se.Id === e.Id;
                        });

                        if (ev.value > 0) {
                            if (probability > (ev.value * 0.1) && curNode.length === 0) {
                                // Add Edge
                                var sourceNode = self.nodes.filter(function (n) {
                                    return n.Id === e.source.Id;
                                })[0];

                                var targetNode = self.nodes.filter(function (n) {
                                    return n.Id === e.target.Id;
                                })[0];

                                self.edges.push({
                                    source: sourceNode,
                                    target: targetNode,
                                    weight: e.weight,
                                    Id: e.Id
                                });
                            }
                            else if (probability <= (ev.value * 0.1)) {
                                self.edges = self.edges.filter(function (d) {
                                    return d.Id != e.Id;
                                });
                            }
                        } else {
                            if (probability < 1 - (Math.abs(ev.value) * 0.1) && curNode.length === 0) {
                                // Add Edge
                                var sourceNode = self.nodes.filter(function (n) {
                                    return n.Id === e.source.Id;
                                })[0];

                                var targetNode = self.nodes.filter(function (n) {
                                    return n.Id === e.target.Id;
                                })[0];

                                self.edges.push({
                                    source: sourceNode,
                                    target: targetNode,
                                    weight: e.weight,
                                    Id: e.Id
                                });
                            } else if (probability >= 1 - (Math.abs(ev.value) * 0.1)) {
                                self.edges = self.edges.filter(function (d) {
                                   return d.Id != e.Id;
                                });
                            }

                        }
                    });
                });
            }

            self.recalcEdgeProbabilities();
            //self.setEdgeSourceTarget(self.edges);
            self.update();
        });


        org_meyerlab_nopence.helper.RetrieveAutomatonFileNames();

        this.width = 1500;
        this.height = 800;
        this.maxRadius = 12;
        this.padding = 20;

        this.color = d3.scale.category20();

        this.force = d3.layout.force()
            .charge(-300)
            .linkDistance(350)
            .size([this.width, this.height]);

        this.svg = d3.select("#showAutomata").append("svg")
            .attr("width", this.width)
            .attr("height", this.height);

        this.init = function(automatonFile) {
            $.getJSON(automatonFile, self.workWithData);

            self.force
                .nodes(self.nodes)
                .links(self.edges)
                .on("tick", self.tick)
                .start();

            // The initial display
            this.update();

            org_meyerlab_nopence.helper.FillMarkAttributes(self.attributes);
        };

        this.tick = function(e) {
            self.circle.attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")";
            });

            self.nodeText.attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")";
            });

            self.path.attr("d", function(d) {
                var x1 = d.source.x,
                    x2 = d.target.x,
                    y1 = d.source.y,
                    y2 = d.target.y;

                var dx = d.target.x - d.source.x,
                    dy = d.target.y - d.source.y,
                    dr = Math.sqrt(dx * dx + dy * dy);

                // x and y distances from center to outside edge of target node
                var offsetX = (dx * (d.target.radius - 15)) / dr;
                var offsetY = (dy * (d.target.radius - 15)) / dr;


                // Defaults for normal edge.
                var drx = dr,
                    dry = dr,
                    xRotation = 0, // degrees
                    largeArc = 0, // 1 or 0
                    sweep = 1; // 1 or 0

                // Self edge.
                if ( x1 === x2 && y1 === y2 ) {
                    // Fiddle with this angle to get loop oriented.
                    xRotation = -85;

                    // Needs to be 1.
                    largeArc = 1;

                    // Change sweep to change orientation of loop.
                    sweep = 0;

                    // Make drx and dry different to get an ellipse
                    // instead of a circle.
                    drx = 30;
                    dry = 30;

                    offsetX = (dx * d.target.radius) / drx;
                    offsetY = (dy * d.target.radius) / dry;

                    // For whatever reason the arc collapses to a point if the beginning
                    // and ending points of the arc are the same, so kludge it.
                    x2 = x2 + 1;
                    y2 = y2 + 1;
                }

                return "M" + x1 + "," + y1 + "A" + drx + "," + dry + " " + xRotation + "," + largeArc + "," + sweep + " " + (x2 - offsetX) + "," + (y2 - offsetY);
            });
        };

        this.setEdgeSourceTarget = function(edges) {
            // Map the target and source nodes to the edge
            edges.forEach(function (e) {
                var sourceNode = self.nodes.filter(function (n) {
                        return n.Id === e.Source;
                    })[0],
                    targetNode = self.nodes.filter(function (n) {
                        return n.Id === e.Target;
                    })[0];

                if (self.overallMaxWeight == null || e.Tds > self.overallMaxWeight) {
                    self.overallMaxWeight = e.Tds;
                }

                if (self.overallMinWeight == null || e.Tds < self.overallMinWeight) {
                    self.overallMinWeight = e.Tds;
                }

                self.edges.push({
                    source: sourceNode,
                    target: targetNode,
                    weight: e.Tds,
                    Id: e.Id
                });
            });
        };

        this.workWithData = function(data){
            self.edges = [];
            self.nodes = data.nodes;
            self.attributes = data.attributes;

            self.overallMinWeight = -1;
            self.overallMaxWeight = 10000000;

            self.setEdgeSourceTarget(data.edges);
            self.originalEdges = $.extend(true, [], self.edges);


            self.nodes.forEach(function (n) {
                n.text = n.Id;
                n.radius = 15;
            });

            self.recalcEdgeProbabilities();
        };

        this.recalcEdgeProbabilities = function() {
            // Calc probabilities
            self.nodes.forEach(function (n) {
                var outEdges = self.edges.filter(function (e) {
                    return n.Id === e.source.Id;
                });

                var sumWeight = 0;
                outEdges.forEach(function (e) {
                    sumWeight += e.weight;
                });

                outEdges.forEach(function (e) {
                    e.probability = e.weight / sumWeight;
                });
            });
        };

        this.update = function() {
            // DATA JOIN
            // Join new data with old elements, if any.
            self.path = self.svg.selectAll("path")
                .data(self.edges,  function(d) { return d.Id; });

            self.circle = self.svg.selectAll("circle")
                .data(self.nodes,  function(d) { return d.Id; });

            self.nodeText = self.svg.selectAll("text")
                .data(self.nodes,  function(d) { return d.Id; });

            self.marker = self.svg.selectAll("marker")
                .data(["suit", "licensing", "resolved"], function(d) { return d; });

            // UPDATE
            // Update old elements as needed
            self.path
                .attr("class", function(d) { return "link licensing"; })
                .attr("marker-end", function(d) { return "url(#licensing)"; })
                .text(function (d) {
                    return d.probability;
                })
                .transition()
                    .duration(750)
                        .style("opacity", function (d) {
                            return d.probability < 0.01 ? 0.1 : d.probability;
                        });

            self.circle
                .transition()
                    .duration(750)
                    .attr("r", function (d) {return d.radius; });

            self.nodeText
                .text(function(d) { return d.text });

            self.marker
                .attr("viewBox", "0 -5 10 10")
                .attr("refX", 22)
                .attr("refY", -1.5)
                .attr("markerWidth", 8)
                .attr("markerHeight", 8)
                .attr("orient", "auto")
                .append("path")
                .attr("d", "M0,-5L10,0L0,5");


            // ENTER
            // Create new elements as needed
            // Per-type markers, as they don't inherit styles.
            self.marker.enter().append("marker")
                .attr("id", function(d) { return d; })
                .attr("viewBox", "0 -5 10 10")
                .attr("refX", 22)
                .attr("refY", -1.5)
                .attr("markerWidth", 8)
                .attr("markerHeight", 8)
                .attr("orient", "auto")
                .append("path")
                .attr("d", "M0,-5L10,0L0,5");

            self.path.enter().append("path")
                .style("opacity", function (d) {
                    return d.probability < 0.01 ? 0.1 : d.probability;
                })
                .attr("class", function(d) { return "link licensing"; })
                .attr("marker-end", function(d) { return "url(#licensing)"; })
                .call(org_meyerlab_nopence.helper.Tooltip(
                    function(d, i){
                        return "Probability: " + d.probability;
                    }
                ));

            self.circle.enter().append("circle")
                .attr("r", function (d) {
                    return d.radius;
                })
                .call(self.force.drag)
                .call(org_meyerlab_nopence.helper.Tooltip(
                    function(d, i){
                        return self.getCircleToolTipText(d);
                    }
                ));

            self.nodeText.enter().append("text")
                .attr("text-anchor", "middle")
                .text(function(d) { return d.Id });

            // EXIT
            // Removes old elements as needed
            self.circle.exit().transition()
                .duration(750)
                .attr("y", 60)
                .style("fill-opacity", 1e-6)
                .remove();
            self.path.exit().transition()
                .duration(750)
                .attr("y", 60)
                .style("fill-opacity", 1e-6)
                .remove();

            self.tick();
        };

        this.getCircleToolTipText = function(node) {
            var toolTipText = "";

            var alreadySeenOriginalAttrs = [];
            node.Attributes.forEach(function (id) {
                var orgAttr = self.attributes.filter(function (a1) {
                    return a1.Id === id;
                })[0];

                if (orgAttr.Type === "nominal") {
                    toolTipText +="<b>Name: </b>" + orgAttr.Name + "</br>"
                } else if (alreadySeenOriginalAttrs.indexOf(orgAttr.OriginalId) === -1) {

                    orgAttr = self.findMaxOrdinalAttr(node, orgAttr.OriginalId);
                    toolTipText += "<b>Name: </b>" + orgAttr.Name + "</br>";
                    alreadySeenOriginalAttrs.push(orgAttr.OriginalId);
                }


            });

            return toolTipText;
        };

        this.findNominalAttr = function(node, orgAttrId) {
            if (node.Attributes === undefined || node.Attributes.length === 0) {
                return undefined;
            }

            node.Attributes.forEach(function (id) {
                var orgAttr = self.attributes.filter(function (s) {
                    return s.Id === id;
                })[0];

                if (orgAttr.OriginalId === orgAttrId) {
                    return orgAttr;
                }
            });
        };

        this.findMaxOrdinalAttr = function(node, orgAttrId) {
            if (node.Attributes === undefined || node.Attributes.length === 0) {
                return undefined;
            }

            var currentMaxOrgOrdinalAttr = undefined;
            node.Attributes.forEach(function (id) {
                var orgAttr = self.attributes.filter(function (s) {
                    return s.Id === id;
                })[0];

                if (orgAttr.OriginalId === orgAttrId) {
                    if (currentMaxOrgOrdinalAttr === undefined
                        || currentMaxOrgOrdinalAttr.Id < orgAttr.Id) {
                        currentMaxOrgOrdinalAttr = orgAttr;
                    }
                }
            });

            return currentMaxOrgOrdinalAttr;
        };
    };

    // Public
    d3Visual.prototype.ShowAutomata = function(fileName) {
        this.init(fileName);
    };

    d3Visual.prototype.markAttr = function(orgAttrId) {
        var self = this;

        var attribute = this.attributes.filter(function (a) {
            return a.OriginalId === orgAttrId;
        })[0];

        if (attribute === undefined) {
            return;
        }

        if (attribute.Type === "nominal") {

            // Show value only in node
            this.nodes.forEach(function (n) {
                var fullAttr = self.findMaxOrdinalAttr(n, orgAttrId);

                if (fullAttr != undefined) {
                    n.text = fullAttr.Name;
                    n.radius = 15;
                }
            });
        } else {
            // Get number of ordinalValues, max and min
            var allAttrIds = [];

            this.attributes.forEach(function (a) {
                if (a.OriginalId === orgAttrId) {
                    allAttrIds.push(a.Id);
                }
            });

            var allMaxId = d3.max(allAttrIds);
            var allMinId = d3.min(allAttrIds);
            var numberOfValues = allAttrIds.length;


            this.nodes.forEach(function (n) {
                var fullOrdMaxAttr = self.findMaxOrdinalAttr(n, orgAttrId);

                if (fullOrdMaxAttr != undefined) {
                    n.text = fullOrdMaxAttr.Name;
                    n.radius = 6 +  ((numberOfValues * 5) / (allMaxId - fullOrdMaxAttr.Id + 1));
                }
            });
        }

        this.update();
    };

    // Export
    org_meyerlab_nopence.d3Visual = new d3Visual();

})();