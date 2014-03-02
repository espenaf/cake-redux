angular.module('cakeReduxModule')
    .factory("eventFactory",["$http", function($http) {
        var res = function (callback) {

            $http({method: "GET", url: "data/events"})
                .success(function(data) {
                    callback(data);
                });

        };
         return res;
    }])
    .factory('talkList',[function() {
        var li = {
            allTalks : [],
            talks: []
        };
        return _.clone(li);
    }])
    .factory('filterService',[function() {
        var isMatch= function(filter,obj) {
            if (filter && filter.length > 0 && obj.toLowerCase().indexOf(filter.toLowerCase()) == -1) {
                return false;
            }
            return true;
        }
        var findInArray=function(filter, arr) {
            if (!filter || filter.length == 0) {
                return true;
            }
            if (!arr) {
                return false;
            }
            var found = false;
            _.each(arr, function(item) {
                if (found) {
                   return;
                }
                if (item.toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
                    found = true;
                }
            });
            return found;
        }

        var matchesFilter = function(talk,filter) {
            if (!isMatch(filter.title,talk.title)) {
                return false;
            }
            if (filter.speaker && filter.speaker.length > 0) {
                var foundSpeaker = false;
                _.each(talk.speakers,function(speaker) {
                    if (foundSpeaker) {
                        return;
                    }

                    if (isMatch(filter.speaker,speaker.name)) {
                        foundSpeaker = true;
                        return;
                    }
                });
                if (!foundSpeaker) {
                    return false;
                }
            }
            if (!isMatch(filter.format,talk.format)) {
                return false;
            }
            if (!isMatch(filter.language,talk.lang)) {
                return false;
            }
            if (!findInArray(filter.tag,talk.tags)) {
                return false;
            }
            if (!findInArray(filter.keyword,talk.keywords)) {
                return false;
            }
            if (!isMatch(filter.state,talk.state)) {
                return false;
            }
            return true;
        }

        var filterOperators = {
            OP_AND: 1,
            OP_OR: 2,
            OP_NOT: 3,
            OP_END: 4
        };

        var doMathing = function myself(talk,filters,from,operator) {
            var match = operator == filterOperators.OP_AND;
            for (var num=from;num<filters.length;num++) {
                var matchesThis;
                if (filters[num].filterOperator) {
                    var useOper = filters[num].filterOperator;
                    if (useOper == filterOperators.OP_END) {
                        return {
                            num: num,
                            match: match
                        };
                    }
                    if (useOper == filterOperators.OP_NOT) {
                        useOper = filterOperators.OP_OR;
                    }
                    var res = myself(talk,filters,num+1,useOper);
                    matchesThis = res.match;
                    if (filters[num].filterOperator == filterOperators.OP_NOT) {
                        matchesThis = !matchesThis;
                    }
                    num=res.num;
                } else {
                    matchesThis=matchesFilter(talk,filters[num]);
                }
                if (operator == filterOperators.OP_AND) {
                    match = match && matchesThis;
                } else {
                    match = match || matchesThis;
                }

            }
            return {
                num: num,
                match: match
            };

        }

        var fis = {
            filters : [],
            filterOperators : filterOperators,
            doFilter : function(talks,allTalks) {
                var self = this;
                talks.splice(0,talks.length);
                _.each(allTalks,function(talk) {
                    var res = doMathing(talk,self.filters,0,filterOperators.OP_OR);
                    if (res.match) {
                        talks.push(talk);
                    }
                });
            }
        };
        return fis;
    }])
;
