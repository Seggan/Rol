-- ROLMETA {"version":1,"package":"rol","dependencies":[],"variables":[{"name":"print","mangled":"rol_print_5982b","type":"(dyn?) -> Void"},{"name":"println","mangled":"rol_println_5982b","type":"(dyn?) -> Void"},{"name":"readLine","mangled":"rol_readLine_5982b","type":"() -> String"},{"name":"readNumber","mangled":"rol_readNumber_5982b","type":"() -> Number"}],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
 local write = io.write 
rol_print_5982b = function (x)
        if x == nil then
            write("null")
        else
            write(x)
        end
end
rol_println_5982b = function (x)
rol_print_5982b(x)
rol_print_5982b("\n")
end
rol_readLine_5982b = function ()
return assertNonNull( io.read("*l") , "file 'rio.rol', line 21, column 11, statement 'extern { io.read(\"*l\") }!'")
end
rol_readNumber_5982b = function ()
return assertNonNull( io.read("*n") , "file 'rio.rol', line 25, column 11, statement 'extern { io.read(\"*n\") }!'")
end