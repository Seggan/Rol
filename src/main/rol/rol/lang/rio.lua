-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"print","mangled":"print_rol_lang_4ef947a3bcfs2f48a873","args":[{"name":"s","type":"dyn?"}],"returnType":"<nothing>"},{"name":"readLine","mangled":"readLine_rol_lang_cf0c6_57572","args":[],"returnType":"String"},{"name":"readNumber","mangled":"readNumber_rol_lang25395a_6798b","args":[],"returnType":"Num"}],"variables":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function print_rol_lang_4ef947a3bcfs2f48a873(s2f48a873)
	if (s2f48a873 == nil) then
		print("null")
	else
		print(s2f48a873)
	end
end
function readLine_rol_lang_cf0c6_57572()
	return assertNonNull(io.read("*l"), "file 'rio.rol', line 16, column 11, statement 'luaRead(\"*l\")!'")
end
function readNumber_rol_lang25395a_6798b()
	return assertNonNull(io.read("*n"), "file 'rio.rol', line 20, column 11, statement 'luaRead(\"*n\")!'")
end