#![allow(dead_code)]
#![allow(nonstandard_style)]
#![allow(unused_braces)]
#![allow(unused_imports)]
#![allow(unused_parens)]
#![allow(unused_variables)]

pub mod lexer {
    include!(concat!(env!("OUT_DIR"), "/rollexer.rs"));
}

pub mod parser {
    include!(concat!(env!("OUT_DIR"), "/rolparser.rs"));
}

include!(concat!(env!("OUT_DIR"), "/rolparservisitor.rs"));
include!(concat!(env!("OUT_DIR"), "/rolparserlistener.rs"));