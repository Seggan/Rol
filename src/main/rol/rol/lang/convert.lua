-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"toString","mangled":"toString_rol_lang_cf0c6_626e1x2f48a878","args":[{"name":"x","type":"dyn?"}],"returnType":"String"},{"name":"toNumber","mangled":"toNumber_rol_lang25395b22d986s_cf0c673","args":[{"name":"s","type":"String"}],"returnType":"Num?"}],"variables":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function toString_rol_lang_cf0c6_626e1x2f48a878(x2f48a878)
	if (x2f48a878 == nil) then
		return "null"
	end
	return assertNonNull(tostring(x2f48a878), "file 'convert.rol', line 10, column 11, statement 'luaToString(x)!'")
end
function toNumber_rol_lang25395b22d986s_cf0c673(s_cf0c673)
	return tonumber(s_cf0c673)
end