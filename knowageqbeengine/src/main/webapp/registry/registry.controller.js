/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
    'use strict';

    angular
        .module('BlankApp', ['ngMaterial', 'registryConfig', 'sbiModule'])
        .config(['$mdThemingProvider', function($mdThemingProvider) {
            $mdThemingProvider.theme('knowage')
            $mdThemingProvider.setDefaultTheme('knowage');
        }])
        .controller('RegistryController', RegistryController)

    function RegistryController(registryConfigService, registryCRUDService) {
        var self = this;
        var registryConfigurationService = registryConfigService;
        var registryCRUD = registryCRUDService;
        var registryConfiguration = registryConfigurationService.getRegistryConfig();
     
        console.log(registryConfiguration);
        
        registryCRUD.read().then(function (response) {
        	 self.data = response.data.rows;
        	
        });
        console.log(registryCRUD.create());
        console.log(registryCRUD.update());
        console.log(registryCRUD.delete()); 
        self.filters = {};
        self.page = 1;

        //array object to define the registry configuration
        self.configuration = {
            title: "Registry Document",
            itemsPerPage: 15,
            enableAdd: true,
            filters: [
                { "label": "first_name", "type": "text" },
                { "label": "gender", "type": "select", "options": [{ "label": "Male", "value": "Male" }, { "label": "Female", "value": "Female" }] }
            ]
        };

        self.isArray = angular.isArray;

        self.deleteRow = function(hash) {
            angular.forEach(self.data, function(value, key) {
                if (value.$$hashKey == hash) {
                    self.data.splice(key, 1);
                    return;
                }
            })
        }

        self.addRow = function() {
            var tmpRow = angular.copy(self.data[0], {});
            for (var i in tmpRow) {
                tmpRow[i] = "";
            };
            self.data.unshift(tmpRow);
        }

        //reordering columns function
        self.move = function(position, direction) {
            var prev, cur, next;
            if (direction == 'left') {
                angular.forEach(self.columns, function(value, key) {
                    if (value.position == (position - 1)) prev = key;
                    if (value.position == (position)) cur = key;
                })
                self.columns[cur].position--;
                self.columns[prev].position++;
            } else {
                angular.forEach(self.columns, function(value, key) {
                    if (value.position == (position + 1)) next = key;
                    if (value.position == (position)) cur = key;
                })
                self.columns[cur].position++;
                self.columns[next].position--;
            }

        }

        self.addToFilters = function(filter) {
            self.filters[filter.label] = filter.value;
        }

        self.getTotalPages = function() {
            return new Array(Math.ceil(self.data.length / self.configuration.itemsPerPage));
        };

        self.hasNext = function() {
            return self.page * self.configuration.itemsPerPage < self.data.length;
        };

        self.hasPrevious = function() {
            return self.page > 1;
        };

        self.min = function() {
            return self.data.length > 0 ? (self.page - 1) * self.configuration.itemsPerPage + 1 : 0;
        };

        self.max = function() {
            return self.hasNext() ? (self.page * self.configuration.itemsPerPage) : self.data.length;
        };

        self.next = function() {
            self.hasNext() && self.page++;
        }

        self.previous = function() {
            self.hasPrevious() && self.page--;
        }


      //  self.columns = registryConfiguration.columns;
        
        //array object to define columns
        //editable makes the column editable in view
        //position manages the position in the columns array
        self.columns = [
            { "label": "id", "name": "id", "position": 2 },
            { "label": "first_name", "name": "first_name", "position": 1, "pivotable": true },
            { "label": "last_name", "name": "last_name", "position": 0, "editable": true, "type": "text" },
            { "label": "email", "name": "email", "position": 3 },
            {
                "label": "gender",
                "name": "gender",
                "position": 4,
                "editable": true,
                "type": "select",
                "options": ["Male", "Female"]
            },
            { "label": "ip_address", "name": "ip_address", "position": 5 }
        ];

        //array object to define data
      /*  self.data = [{ "id": 1, "first_name": "Kirsti", "last_name": "Michiel", "email": "kmichiel0@ask.com", "gender": "Female", "ip_address": "134.68.23.46" },
            { "id": 2, "first_name": ["Terenzio", "Roberto", "Marco"], "last_name": "Ashpital", "email": "hashpital1@typepad.com", "gender": "Male", "ip_address": "185.147.156.173" },
            { "id": 3, "first_name": "Debi", "last_name": "Willingam", "email": "dwillingam2@list-manage.com", "gender": "Female", "ip_address": "90.227.251.249" },
            { "id": 4, "first_name": "Teresita", "last_name": "Giannazzo", "email": "tgiannazzo3@irs.gov", "gender": "Female", "ip_address": "172.254.81.159" },
            { "id": 5, "first_name": "Port", "last_name": "Pitsall", "email": "ppitsall4@acquirethisname.com", "gender": "Male", "ip_address": "69.240.155.147" },
            { "id": 6, "first_name": "Deeanne", "last_name": "Conibear", "email": "dconibear5@github.com", "gender": "Female", "ip_address": "2.171.8.105" },
            { "id": 7, "first_name": "Sammie", "last_name": "Ryves", "email": "sryves6@shinystat.com", "gender": "Male", "ip_address": "43.248.73.70" },
            { "id": 8, "first_name": "Xena", "last_name": "Kubelka", "email": "xkubelka7@si.edu", "gender": "Female", "ip_address": "220.153.96.184" },
            { "id": 9, "first_name": "Howard", "last_name": "Daft", "email": "hdaft8@webnode.com", "gender": "Male", "ip_address": "45.56.85.194" },
            { "id": 10, "first_name": "Brittan", "last_name": "Finnan", "email": "bfinnan9@washington.edu", "gender": "Female", "ip_address": "206.221.47.66" },
            { "id": 11, "first_name": "Carolus", "last_name": "Van Arsdalen", "email": "cvanarsdalena@noaa.gov", "gender": "Male", "ip_address": "212.17.124.46" },
            { "id": 12, "first_name": "Chere", "last_name": "Trask", "email": "ctraskb@dot.gov", "gender": "Female", "ip_address": "178.234.108.228" },
            { "id": 13, "first_name": "Carce", "last_name": "Turtle", "email": "cturtlec@cpanel.net", "gender": "Male", "ip_address": "143.239.130.0" },
            { "id": 14, "first_name": "Sherlocke", "last_name": "Staries", "email": "sstariesd@comsenz.com", "gender": "Male", "ip_address": "100.253.8.122" },
            { "id": 15, "first_name": "Bryn", "last_name": "Ilyas", "email": "bilyase@wikispaces.com", "gender": "Male", "ip_address": "69.80.71.209" },
            { "id": 16, "first_name": "Patti", "last_name": "Dogg", "email": "pdoggf@senate.gov", "gender": "Female", "ip_address": "127.12.83.183" },
            { "id": 17, "first_name": "Gris", "last_name": "Kindall", "email": "gkindallg@foxnews.com", "gender": "Male", "ip_address": "59.224.116.169" },
            { "id": 18, "first_name": "Wesley", "last_name": "Doram", "email": "wdoramh@cisco.com", "gender": "Male", "ip_address": "233.63.132.185" },
            { "id": 19, "first_name": "Rozella", "last_name": "Burdis", "email": "rburdisi@mediafire.com", "gender": "Female", "ip_address": "158.11.4.10" },
            { "id": 20, "first_name": "Englebert", "last_name": "Matous", "email": "ematousj@google.com", "gender": "Male", "ip_address": "46.38.208.238" },
            { "id": 21, "first_name": "Dre", "last_name": "Bergstrand", "email": "dbergstrandk@hubpages.com", "gender": "Female", "ip_address": "110.142.91.61" },
            { "id": 22, "first_name": "Donny", "last_name": "Swapp", "email": "dswappl@netvibes.com", "gender": "Female", "ip_address": "79.223.157.68" },
            { "id": 23, "first_name": "Linus", "last_name": "Sivill", "email": "lsivillm@xrea.com", "gender": "Male", "ip_address": "93.114.31.143" },
            { "id": 24, "first_name": "Lonnie", "last_name": "Probbing", "email": "lprobbingn@scientificamerican.com", "gender": "Female", "ip_address": "155.216.21.250" },
            { "id": 25, "first_name": "Mycah", "last_name": "Larway", "email": "mlarwayo@reference.com", "gender": "Male", "ip_address": "181.195.72.0" },
            { "id": 26, "first_name": "Terza", "last_name": "Gouth", "email": "tgouthp@flavors.me", "gender": "Female", "ip_address": "106.72.245.176" },
            { "id": 27, "first_name": "Darby", "last_name": "Dallinder", "email": "ddallinderq@hhs.gov", "gender": "Female", "ip_address": "231.79.1.24" },
            { "id": 28, "first_name": "Denny", "last_name": "Olivella", "email": "dolivellar@ameblo.jp", "gender": "Male", "ip_address": "239.244.234.119" },
            { "id": 29, "first_name": "Merrilee", "last_name": "Howgill", "email": "mhowgills@xinhuanet.com", "gender": "Female", "ip_address": "15.103.205.119" },
            { "id": 30, "first_name": "Thornie", "last_name": "Garioch", "email": "tgariocht@abc.net.au", "gender": "Male", "ip_address": "149.34.44.180" },
            { "id": 31, "first_name": "Meridel", "last_name": "Clubb", "email": "mclubbu@toplist.cz", "gender": "Female", "ip_address": "86.194.71.149" },
            { "id": 32, "first_name": "Gayelord", "last_name": "McAuliffe", "email": "gmcauliffev@desdev.cn", "gender": "Male", "ip_address": "198.130.32.3" },
            { "id": 33, "first_name": "Sauncho", "last_name": "Vezey", "email": "svezeyw@apache.org", "gender": "Male", "ip_address": "43.127.150.14" },
            { "id": 34, "first_name": "Crawford", "last_name": "Inkle", "email": "cinklex@wisc.edu", "gender": "Male", "ip_address": "183.173.72.243" },
            { "id": 35, "first_name": "Mahmoud", "last_name": "Weale", "email": "mwealey@si.edu", "gender": "Male", "ip_address": "151.141.1.125" },
            { "id": 36, "first_name": "Giulietta", "last_name": "Harder", "email": "gharderz@squarespace.com", "gender": "Female", "ip_address": "88.102.180.177" },
            { "id": 37, "first_name": "Keene", "last_name": "Deamer", "email": "kdeamer10@europa.eu", "gender": "Male", "ip_address": "206.243.247.185" },
            { "id": 38, "first_name": "Emory", "last_name": "Kippin", "email": "ekippin11@epa.gov", "gender": "Male", "ip_address": "145.204.78.161" },
            { "id": 39, "first_name": "Brandise", "last_name": "Engall", "email": "bengall12@wordpress.com", "gender": "Female", "ip_address": "158.37.132.139" },
            { "id": 40, "first_name": "Tremayne", "last_name": "Calder", "email": "tcalder13@google.co.uk", "gender": "Male", "ip_address": "42.216.162.16" },
            { "id": 41, "first_name": "Dalia", "last_name": "Cuer", "email": "dcuer14@furl.net", "gender": "Female", "ip_address": "207.98.193.249" },
            { "id": 42, "first_name": "Sandro", "last_name": "Sivior", "email": "ssivior15@naver.com", "gender": "Male", "ip_address": "181.210.213.95" },
            { "id": 43, "first_name": "Alexia", "last_name": "Le Hucquet", "email": "alehucquet16@g.co", "gender": "Female", "ip_address": "250.75.160.43" },
            { "id": 44, "first_name": "Gilles", "last_name": "Whiteoak", "email": "gwhiteoak17@umich.edu", "gender": "Male", "ip_address": "190.118.251.241" },
            { "id": 45, "first_name": "Buddy", "last_name": "Petley", "email": "bpetley18@wired.com", "gender": "Male", "ip_address": "103.125.27.225" },
            { "id": 46, "first_name": "Chan", "last_name": "Rantoul", "email": "crantoul19@tripadvisor.com", "gender": "Male", "ip_address": "197.105.140.159" },
            { "id": 47, "first_name": "Linnet", "last_name": "Edwicker", "email": "ledwicker1a@infoseek.co.jp", "gender": "Female", "ip_address": "133.70.55.103" },
            { "id": 48, "first_name": "Tobe", "last_name": "Hurdle", "email": "thurdle1b@diigo.com", "gender": "Male", "ip_address": "127.59.57.234" },
            { "id": 49, "first_name": "Gabriell", "last_name": "Cholmondeley", "email": "gcholmondeley1c@infoseek.co.jp", "gender": "Female", "ip_address": "41.179.238.55" },
            { "id": 50, "first_name": "Temple", "last_name": "Ubach", "email": "tubach1d@forbes.com", "gender": "Male", "ip_address": "232.86.128.1" },
            { "id": 51, "first_name": "Odell", "last_name": "Chatenet", "email": "ochatenet1e@furl.net", "gender": "Male", "ip_address": "37.113.17.58" },
            { "id": 52, "first_name": "Sandra", "last_name": "Glastonbury", "email": "sglastonbury1f@ucsd.edu", "gender": "Female", "ip_address": "2.175.58.250" },
            { "id": 53, "first_name": "Quintana", "last_name": "Malenfant", "email": "qmalenfant1g@narod.ru", "gender": "Female", "ip_address": "88.61.75.165" },
            { "id": 54, "first_name": "Moses", "last_name": "Brame", "email": "mbrame1h@barnesandnoble.com", "gender": "Male", "ip_address": "52.112.209.101" },
            { "id": 55, "first_name": "Lionello", "last_name": "Henaughan", "email": "lhenaughan1i@weebly.com", "gender": "Male", "ip_address": "101.209.103.95" },
            { "id": 56, "first_name": "Leisha", "last_name": "Rushsorth", "email": "lrushsorth1j@europa.eu", "gender": "Female", "ip_address": "34.71.242.44" },
            { "id": 57, "first_name": "Millie", "last_name": "Blumson", "email": "mblumson1k@google.com.hk", "gender": "Female", "ip_address": "193.57.19.76" },
            { "id": 58, "first_name": "Moritz", "last_name": "Scampion", "email": "mscampion1l@fema.gov", "gender": "Male", "ip_address": "222.37.75.252" },
            { "id": 59, "first_name": "Berget", "last_name": "Atthowe", "email": "batthowe1m@prweb.com", "gender": "Female", "ip_address": "218.87.245.197" },
            { "id": 60, "first_name": "Cad", "last_name": "Hayton", "email": "chayton1n@imdb.com", "gender": "Male", "ip_address": "5.163.52.247" },
            { "id": 61, "first_name": "Aharon", "last_name": "Stoppard", "email": "astoppard1o@macromedia.com", "gender": "Male", "ip_address": "109.93.173.98" },
            { "id": 62, "first_name": "Orelle", "last_name": "Pauwel", "email": "opauwel1p@archive.org", "gender": "Female", "ip_address": "52.74.196.48" },
            { "id": 63, "first_name": "Klemens", "last_name": "Burnsell", "email": "kburnsell1q@marriott.com", "gender": "Male", "ip_address": "194.129.58.63" },
            { "id": 64, "first_name": "Christos", "last_name": "Cammacke", "email": "ccammacke1r@nifty.com", "gender": "Male", "ip_address": "119.186.114.251" },
            { "id": 65, "first_name": "Bran", "last_name": "Firmage", "email": "bfirmage1s@statcounter.com", "gender": "Male", "ip_address": "12.96.213.167" },
            { "id": 66, "first_name": "Dulcy", "last_name": "Fireman", "email": "dfireman1t@pen.io", "gender": "Female", "ip_address": "101.101.9.83" },
            { "id": 67, "first_name": "Kalie", "last_name": "Spencelayh", "email": "kspencelayh1u@businesswire.com", "gender": "Female", "ip_address": "238.142.120.112" },
            { "id": 68, "first_name": "Denney", "last_name": "Willcocks", "email": "dwillcocks1v@dailymotion.com", "gender": "Male", "ip_address": "12.46.146.139" },
            { "id": 69, "first_name": "Urson", "last_name": "Lacase", "email": "ulacase1w@chronoengine.com", "gender": "Male", "ip_address": "39.238.122.38" },
            { "id": 70, "first_name": "Jaime", "last_name": "Leyre", "email": "jleyre1x@quantcast.com", "gender": "Male", "ip_address": "5.22.185.122" },
            { "id": 71, "first_name": "Charisse", "last_name": "Petrushanko", "email": "cpetrushanko1y@barnesandnoble.com", "gender": "Female", "ip_address": "73.208.15.136" },
            { "id": 72, "first_name": "Porter", "last_name": "Casarini", "email": "pcasarini1z@liveinternet.ru", "gender": "Male", "ip_address": "136.13.224.12" },
            { "id": 73, "first_name": "Benji", "last_name": "Ahrend", "email": "bahrend20@howstuffworks.com", "gender": "Male", "ip_address": "185.207.173.34" },
            { "id": 74, "first_name": "Ezmeralda", "last_name": "Grinikhinov", "email": "egrinikhinov21@wisc.edu", "gender": "Female", "ip_address": "219.246.67.111" },
            { "id": 75, "first_name": "Leda", "last_name": "Algate", "email": "lalgate22@ifeng.com", "gender": "Female", "ip_address": "26.172.116.18" },
            { "id": 76, "first_name": "Bird", "last_name": "Hedges", "email": "bhedges23@dmoz.org", "gender": "Female", "ip_address": "52.64.130.41" },
            { "id": 77, "first_name": "Abdul", "last_name": "Paquet", "email": "apaquet24@amazon.co.uk", "gender": "Male", "ip_address": "64.238.112.84" },
            { "id": 78, "first_name": "Pierrette", "last_name": "Laverock", "email": "plaverock25@cbc.ca", "gender": "Female", "ip_address": "21.99.163.48" },
            { "id": 79, "first_name": "Halette", "last_name": "Cordero", "email": "hcordero26@vimeo.com", "gender": "Female", "ip_address": "56.128.76.70" },
            { "id": 80, "first_name": "Janella", "last_name": "Pollington", "email": "jpollington27@sogou.com", "gender": "Female", "ip_address": "183.94.25.80" },
            { "id": 81, "first_name": "Clemmy", "last_name": "Dearsley", "email": "cdearsley28@unblog.fr", "gender": "Male", "ip_address": "205.181.97.15" },
            { "id": 82, "first_name": "Flem", "last_name": "Trytsman", "email": "ftrytsman29@domainmarket.com", "gender": "Male", "ip_address": "27.236.172.68" },
            { "id": 83, "first_name": "Larina", "last_name": "Gouldsmith", "email": "lgouldsmith2a@hatena.ne.jp", "gender": "Female", "ip_address": "171.239.174.185" },
            { "id": 84, "first_name": "Kirsti", "last_name": "McClements", "email": "kmcclements2b@spiegel.de", "gender": "Female", "ip_address": "245.255.116.218" },
            { "id": 85, "first_name": "Farand", "last_name": "Relton", "email": "frelton2c@sourceforge.net", "gender": "Female", "ip_address": "109.42.243.202" },
            { "id": 86, "first_name": "Thorvald", "last_name": "McGaugey", "email": "tmcgaugey2d@alexa.com", "gender": "Male", "ip_address": "132.95.179.255" },
            { "id": 87, "first_name": "Meggi", "last_name": "Silversmidt", "email": "msilversmidt2e@aol.com", "gender": "Female", "ip_address": "125.144.5.2" },
            { "id": 88, "first_name": "Juliet", "last_name": "Cabrera", "email": "jcabrera2f@seesaa.net", "gender": "Female", "ip_address": "130.83.200.117" },
            { "id": 89, "first_name": "Donia", "last_name": "Wloch", "email": "dwloch2g@blinklist.com", "gender": "Female", "ip_address": "193.245.12.208" },
            { "id": 90, "first_name": "Therese", "last_name": "Sterry", "email": "tsterry2h@slate.com", "gender": "Female", "ip_address": "172.71.20.191" },
            { "id": 91, "first_name": "Link", "last_name": "Willmer", "email": "lwillmer2i@businessweek.com", "gender": "Male", "ip_address": "140.138.58.95" },
            { "id": 92, "first_name": "Culver", "last_name": "Whimpenny", "email": "cwhimpenny2j@bing.com", "gender": "Male", "ip_address": "213.117.251.185" },
            { "id": 93, "first_name": "Gabbi", "last_name": "Warlton", "email": "gwarlton2k@newsvine.com", "gender": "Female", "ip_address": "183.45.179.66" },
            { "id": 94, "first_name": "Thebault", "last_name": "Dick", "email": "tdick2l@privacy.gov.au", "gender": "Male", "ip_address": "46.174.187.200" },
            { "id": 95, "first_name": "Ardelle", "last_name": "Raithby", "email": "araithby2m@marketwatch.com", "gender": "Female", "ip_address": "79.141.39.187" },
            { "id": 96, "first_name": "Giulio", "last_name": "Blackmuir", "email": "gblackmuir2n@webs.com", "gender": "Male", "ip_address": "250.155.78.0" },
            { "id": 97, "first_name": "Galvin", "last_name": "Symcoxe", "email": "gsymcoxe2o@fema.gov", "gender": "Male", "ip_address": "107.139.68.83" },
            { "id": 98, "first_name": "Woodman", "last_name": "Belli", "email": "wbelli2p@imageshack.us", "gender": "Male", "ip_address": "224.174.250.156" },
            { "id": 99, "first_name": "Shep", "last_name": "Seifert", "email": "sseifert2q@businesswire.com", "gender": "Male", "ip_address": "226.17.52.222" },
            { "id": 100, "first_name": "Bessy", "last_name": "Winsiowiecki", "email": "bwinsiowiecki2r@google.cn", "gender": "Female", "ip_address": "238.16.200.165" }

        ]; */
    }
})();