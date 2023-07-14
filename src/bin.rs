use std::env;
use std::fs::read_to_string;
use std::path::Path;

use rol::process_file;

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if let Some(file) = args.get(1) {
        let file = Path::new(file);
        let text = read_to_string(file).unwrap();
        process_file(file, &text).unwrap_or_else(|e| e.report_and_exit(file, &text));
    } else {
        eprintln!("No input file")
    }
}
