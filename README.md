# Rol

***THE LANGUAGE IS STILL BEING DEVELOPED, MOST FEATURES DON'T YET WORK***

Do you hate Lua's arcane syntax, verbose keywords, and the difficulty of OOP? Do you want a safe language with the expressiveness of modern programming languages? Do you wish to use a different language but are forced by your platform to use Lua? Rol is just for you.

Rol is a programming language that compiles to Lua, do you don't need to fear moving to a different platform. It has many of the features of more modern programming languages, as well as a modern syntax. It is statically typed (although that can be disabled), but tries to infer as many types as possible, so they don't get in your way.

See the `docs` directory for more info and syntax examples.

## Usage
Rol programs are compiled/run using the command `java -jar Rol-<version>.jar [OPTIONS] FILE`, where `FILE` is the input file.

List of options:

| Name              | Short Name | Description                                                               |
|-------------------|------------|---------------------------------------------------------------------------|
| `--output PATH`   | `-o PATH`  | Sets the output file to `PATH`                                            |
| `--include PATHS` | `-I PATHS` | Adds additional directories to search for dependencies                    |
| `--interpret`     | `-i`       | A flag that makes the compiler interpret the file instead of compiling it |
| `--help`          | `-h`       | Shows the help message                                                    |