use std::{env, fs};
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
    let out = env::var("OUT_DIR").unwrap();
    let out = out.as_str();
    let file_names = vec!["rollexer.rs", "rolparser.rs", "rolparservisitor.rs", "rolparserlistener.rs"];

    let current_path = env::current_dir().unwrap();
    let grammar_file = current_path.join("grammars").join(name.to_owned() + ".g4");

    let command = Command::new("java")
        .current_dir(current_path)
        .arg("-cp")
        .arg(ANTLR_PATH)
        .arg("org.antlr.v4.Tool")
        .arg("-Dlanguage=Rust")
        .arg("-o")
        .arg(out)
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

    // Touch up the generated code a bit
    for file in fs::read_dir(out).unwrap() {
        let file = file.unwrap().path();
        if file.is_dir() {
            continue
        }
        if file_names.contains(&file.file_name().unwrap().to_str().unwrap()) {
            let contents = fs::read_to_string(&file).unwrap();
            let new_contents = contents.lines()
                .skip_while(|line| !line.starts_with("use "))
                .map(|line| if line.starts_with("use ") {
                    line.replace("super::rolparser::", "crate::parsing::antlr::parser::")
                        .replace("super::rolparservisitor::", "crate::parsing::antlr::")
                        .replace("super::rolparserlistener::", "crate::parsing::antlr::")
                } else {
                    line.into()
                })
                .collect::<Vec<_>>()
                .join("\n");
            fs::write(file, new_contents).unwrap();
        }
    }

    println!("cargo:rerun-if-changed={}", grammar_file.to_str().unwrap());
}
