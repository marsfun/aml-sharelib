package io.alauda.ml

// def getChangeVersionShellScript(){
//  return """
//  #!/usr/bin/env bash

// changeVersionByUser()
// {
//     context=”\$1”
//     version="\$2"
   
//     for f in `ls "\$context"`; do
//         if [ -d "\$context/\$f" ]; then
//             hasWanted "\$context/\$f" \$version
//             changeVersionByUser "\$context/\$f" "\$version"
//         fi
//     done 
// }
// hasWanted()
// {
//     pbfile=0 
//     varsfolder=0
//     folder=""
//     for ff in `ls "\$1"`; do
//         if [[ -d "\$1/\$ff" && "\$ff"x = "variables"x ]]; then
//             varsfolder=1
//         fi
//         if [ -f "\$1/\$ff" ]; then
//             FILE="\$1/\$ff"
//             extension=\$(echo "\$FILE" | cut -d . -f2)
//             if [[ "pb"x = "\$extension"x ]]; then
//                 pbfile=1
//             fi
//         fi
//         if [ \$pbfile -eq 1 -a \$varsfolder -eq 1 ] ; then 
//             echo \$1
//         fi
//     done
// }
// nnn=\$(changeVersionByUser \$1 \$2)
// echo \$nnn
// """   
// }

def getModelVersionContextPath( orig, version){
    def spliter = "/"
    def arr = orig.split(spliter)

    if (arr.length<1){
        return orig
    }
    arr[arr.length-1] = version
    for(String in arr){

    }
    // return "".join("/",arr)
   
    def result=""
    arr.eachWithIndex { vv,index->
        result+=vv
        if (index!=arr.size()-1){
            result+=spliter
        }
    }
    return result
}