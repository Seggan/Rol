-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"print","mangled":"print_4ef9465fb2as2f48a873","args":[{"name":"s","type":"dyn?"}],"returnType":"<nothing>"},{"name":"readLine","mangled":"readLine_cf0c6_33b93","args":[],"returnType":"String"},{"name":"readNumber","mangled":"readNumber25395a_264ce","args":[],"returnType":"Num"}],"variables":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function print_4ef9465fb2as2f48a873(s2f48a873)
	if (s2f48a873 == nil) then
		print("null")
	else
		print(s2f48a873)
	end
end
function readLine_cf0c6_33b93()
	return assertNonNull(io.read("*l"), "file 'rio.rol', line 16, column 11, statement 'luaRead(\"*l\")!'")
end
function readNumber25395a_264ce()
	return assertNonNull(io.read("*n"), "file 'rio.rol', line 20, column 11, statement 'luaRead(\"*n\")!'")
end