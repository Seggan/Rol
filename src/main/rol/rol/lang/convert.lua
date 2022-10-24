-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"toString","mangled":"toString_cf0c6_69e9ax2f48a878","args":[{"name":"x","type":"dyn?"}],"returnType":"String"},{"name":"toNumber","mangled":"toNumber25395b_72663s_cf0c673","args":[{"name":"s","type":"String"}],"returnType":"Num?"}],"variables":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function toString_cf0c6_69e9ax2f48a878(x2f48a878)
	if (x2f48a878 == nil) then
		return "null"
	end
	return assertNonNull(tostring(x2f48a878), "file 'convert.rol', line 10, column 11, statement 'luaToString(x)!'")
end
function toNumber25395b_72663s_cf0c673(s_cf0c673)
	return tonumber(s_cf0c673)
end