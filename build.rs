use std::env;
use std::process::Command;

fn main() {
    let grammars = vec!["RolLexer", "RolParser"];

    for grammar in grammars {
        gen_grammar(grammar);
    }

    println!("cargo:rerun-if-changed=build.rs");
    println!("cargo:rerun-if-changed={}", ANTLR_PATH);
}

const ANTLR_PATH: &str = "antlr4-4.8-2-SNAPSHOT.jar";

fn gen_grammar(name: &str) {
    let current_path = env::current_dir().unwrap();
    let grammar_file = current_path.join("grammars").join(name.to_owned() + ".g4");

    let command = Command::new("java")
        .current_dir(current_path)
        .arg("-cp")
        .arg(ANTLR_PATH)
        .arg("org.antlr.v4.Tool")
        .arg("-Dlanguage=Rust")
        .arg("-o")
        .arg(env::var("OUT_DIR").unwrap())
        .arg(&grammar_file)
        .arg("-visitor")
        .spawn()
        .expect("ANTLR did not start")
        .wait_with_output()
        .unwrap();

    println!("{}", String::from_utf8(command.stdout).unwrap());

    if !command.status.success() {
        panic!("{}", String::from_utf8(command.stderr).unwrap());
    }

    println!("cargo:rerun-if-changed={}", grammar_file.to_str().unwrap());
}
