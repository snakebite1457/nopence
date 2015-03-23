/**
 * Created by Dennis Meyer on 01.03.2015.
 */

var org_meyerlab_nopence = org_meyerlab_nopence || {};


(function() {

    var d3BarVisual = function() {

        var self = this;

        this.m = [20, 15, 1, 170];
        this.w = 440 - this.m[1] - this.m[3];
        this.h = 170 - this.m[0] - this.m[2];

        this.format = d3.format(",.0f");

        this.x = d3.scale.linear().range([0, this.w]);
        this.y = d3.scale.ordinal().rangeRoundBands([0, this.h], .1);

        this.xAxis = d3.svg.axis().scale(this.x).orient("top").ticks(5);
        this.yAxis = d3.svg.axis().scale(this.y).orient("left").tickSize(0);

        this.update = function(exists) {
            // nodeId: Id, attributes: [{name: abc, values: [{name: abc, prob:0.12}]}, usw]

            this.attributes.forEach(function (attr) {
                if (attr.type === 'nominal') {
                    self.createVerticalBarChart(attr);
                } else {
                    self.createVerticalHistogrammBarChart(attr);
                }
            });
        };

        this.createVerticalBarChart = function(attribute) {
            var li = self.ul
                .append("li");

            li.append("label")
                .attr("class", "tree-toggler nav-header")
                .text("Attr: " + attribute.name)
                .on("click", function (d) {
                    $(this).parent().children('ul.tree').toggle(300)
                });

            var ul = li.append("ul")
                .style("display", "none")
                .attr("class", "nav nav-list tree");

            var svg = ul.append("svg")
                .attr("width", self.w + self.m[1] + self.m[3])
                .attr("height", self.h + self.m[0] + self.m[2])
                .append("g")
                .attr("transform", "translate(" + self.m[3] + "," + self.m[0] + ")");


            attribute.values.sort(function(a, b)
            {
                return b.prob - a.prob;
            });

            // Set the scale domain.
            self.x.domain([0, d3.max(attribute.values, function(d) { return d.prob; })]);
            self.y.domain(attribute.values.map(function(d) { return d.name; }));

            var bar = svg.selectAll("g.bar")
                .data(attribute.values)
                .enter().append("g")
                .attr("class", "bar")
                .attr("transform", function(d) {
                    return "translate(0," + self.y(d.name) + ")";
                });

            bar.append("rect")
                .attr("width", function(d) {
                    return self.x(d.prob);
                })
                .attr("height", self.y.rangeBand());

            bar.append("text")
                .attr("class", "value")
                .attr("x", function(d) { return self.x(0.05); })
                .attr("y", self.y.rangeBand() / 2)
                .attr("dx", -3)
                .attr("dy", ".35em")
                .attr("text-anchor", "start")
                .text(function(d) { return d.prob; });

            svg.append("g")
                .attr("class", "x axis")
                .call(self.xAxis);

            svg.append("g")
                .attr("class", "y axis")
                .call(self.yAxis);
        };

        this.createVerticalHistogrammBarChart = function(attribute) {
            var li = self.ul
                .append("li");

            li.append("label")
                .attr("class", "tree-toggler nav-header")
                .text("Attr: " + attribute.name)
                .on("click", function (d) {
                    $(this).parent().children('ul.tree').toggle(300)
                });

            var ul = li.append("ul")
                .style("display", "none")
                .attr("class", "nav nav-list tree");

            var svg = ul.append("svg")
                .attr("width", self.w + self.m[1] + self.m[3])
                .attr("height", self.h + self.m[0] + self.m[2])
                .append("g")
                .attr("transform", "translate(" + self.m[3] + "," + self.m[0] + ")");


            attribute.values.sort(function(a, b)
            {
                return a.Id - b.Id;
            });

            var density = this.estimateDensity(attribute);

            // Set the scale domain.
            self.x.domain([0, d3.max(density, function(d) { return d.histoProb; })]);
            self.y.domain(attribute.values.map(function(d) { return d.name; }));

            var bar = svg.selectAll("g.bar")
                .data(density)
                .enter().append("g")
                .attr("class", "bar")
                .attr("transform", function(d) {
                    return "translate(0," + self.y(d.value.name) + ")";
                });

            bar.append("rect")
                .attr("width", function(d) {
                    return self.x(d.histoProb);
                })
                .attr("height", self.y.rangeBand());

            bar.append("text")
                .attr("class", "value")
                .attr("x", function(d) { return self.x(0.05); })
                .attr("y", self.y.rangeBand() / 2)
                .attr("dx", -3)
                .attr("dy", ".35em")
                .attr("text-anchor", "start")
                .text(function(d) { return d.histoProb; });

            svg.append("g")
                .attr("class", "x axis")
                .call(self.xAxis);

            svg.append("g")
                .attr("class", "y axis")
                .call(self.yAxis);
        };

        this.estimateDensity = function(attribute) {
            var density = [];

            for(var a = 0; a < attribute.values.length; a++) {
                if (a == attribute.values.length - 1) {
                    density.push({
                        value: attribute.values[a],
                        histoProb: attribute.values[a].prob
                    });
                    continue;
                }

                var probNextValue = attribute.values[a + 1].prob;
                var histoProb = attribute.values[a].prob - probNextValue;
                density.push({
                    value: attribute.values[a],
                    histoProb:  Math.round(histoProb * 100) / 100
                });
            }

            return density;
        };
    };

    d3BarVisual.prototype.AddBars = function (data) {

        var self = this;

        this.numInstances = data.numInstances;
        this.nodeId = data.nodeId;
        this.attributes = data.attributes;

        // Check if node dist already exist
        if (!d3.select("#li" + this.nodeId).empty()) {
            //self.update();
            return;
        }

        // Append new sidebar
        var li = d3.select("#navNodeDist")
            .append("ul")
            .attr("class", "nav nav-list tree")
            .attr("id", "li" + this.nodeId);


        li.append("label")
            .attr("class", "tree-toggler nav-header")
            .text("Node: " + self.nodeId + " Num Instances: " + self.numInstances)
            .on("click", function (d) {
                $(this).parent().children('ul.tree').toggle(300)
            });

        this.ul = li.append("ul")
            .style("display", "none")
            .attr("id", "ul" + this.nodeId)
            .attr("class", "nav nav-list tree");

        this.update(false);
    };

    // Export
    org_meyerlab_nopence.d3BarVisual = new d3BarVisual();

})();
