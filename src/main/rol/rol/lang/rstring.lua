-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"subs","mangled":"subs_cf0c6360a33s_cf0c673start25395a68ac46end25395a188db","args":[{"name":"s","type":"String"},{"name":"start","type":"Num"},{"name":"end","type":"Num"}],"returnType":"String"}],"variables":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function subs_cf0c6360a33s_cf0c673start25395a68ac46end25395a188db(s_cf0c673, start25395a68ac46, end25395a188db)
	return assertNonNull(string.sub(s_cf0c673, (start25395a68ac46 + 1), end25395a188db), "file 'rstring.rol', line 6, column 11, statement 'luaSub(s, start - 1, end)!'")
end