-- ROLMETA {"version":1,"package":"rol","dependencies":[],"variables":[{"name":"print","mangled":"rol_print_32113","type":"(dyn?) -> Void"},{"name":"readLine","mangled":"rol_readLine_32113","type":"() -> String"},{"name":"readNumber","mangled":"rol_readNumber_32113","type":"() -> Number"}],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
rol_print_32113 = function (x)
        if x == nil then
            print("null")
        else
            print(x)
        end
end
rol_readLine_32113 = function ()
return assertNonNull( io.read("*l") , "file 'rio.rol', line 14, column 11, statement 'extern { io.read(\"*l\") }!'")
end
rol_readNumber_32113 = function ()
return assertNonNull( io.read("*n") , "file 'rio.rol', line 18, column 11, statement 'extern { io.read(\"*n\") }!'")
end