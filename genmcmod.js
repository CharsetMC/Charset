var inf = "modules.json"
  , outf = "src/main/resources/mcmod.info"
  , fs = require("fs");

var modules = JSON.parse(fs.readFileSync(inf, "UTF-8"));
var mcmod = Object.keys(modules).map(function(k) {
	var v = modules[k];
	var o = {
		"modid": "Charset" + k.substring(0, 1).toUpperCase() + k.substring(1),
		"name": v.char,
		"version": "${version}",
		"mcversion": "${mcversion}",
		"description": v.description,
		"authorList": [ "asiekierka" ],
		"url": "http://charset.asie.pl"
	};
	return o;
});

fs.writeFileSync(outf, JSON.stringify({
	"modListVersion": 2,
	"modList": mcmod
}, null, 4), "UTF-8");
