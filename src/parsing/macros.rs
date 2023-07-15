/// Reports all errors found in the `try_consume!` macro.
#[macro_export]
macro_rules! consumes_end {
    ($errors:expr) => {
        if !$errors.is_empty() {
            return Result::Err($crate::error::RolError::Multiple($errors));
        }
    };

    ($errors:expr, $sub:expr) => {
        match $sub {
            Result::Ok(val) => {
                consumes_end!($errors);
                val
            },
            Result::Err(err) => {
                $errors.push(err);
                return Result::Err($crate::error::RolError::Multiple($errors));
            }
        }
    };
}

/// Attempts to consume a token from the token stream, returning an error if the token is not found.
#[macro_export]
macro_rules! try_consume {
    ($toks:expr, $tok:pat, $err:literal) => {
        if let Option::Some(next) = $toks.next() {
            match next.token_type {
                $tok => next.clone(),
                _ => {
                    let span = next.span.clone();
                    return Result::Err($crate::error::SyntaxError::ExpectedToken(
                        $err.to_string(),
                        span
                    ).into())
                }
            }
        } else {
            return Result::Err($crate::error::SyntaxError::UnexpectedEof.into());
        }
    };

    ($toks:expr, $errors:expr, $tok:pat, $err:literal) => {
        if let Option::Some(next) = &$toks.next() {
            match next.token_type {
                $tok => {},
                _ => {
                    let span = next.span.clone();
                    $errors.push($crate::error::SyntaxError::ExpectedToken(
                        $err.to_string(),
                        span
                    ).into());
                }
            }
        } else {
            return Result::Err($crate::error::SyntaxError::UnexpectedEof.into());
        }
    };
}

#[macro_export]
macro_rules! match_next {
    ($toks:expr, $tok:pat) => {
        if let Option::Some(next) = $toks.next() {
            if let $tok = next.token_type {
                Option::Some(next.clone())
            } else {
                $toks.previous();
                Option::None
            }
        } else {
            Option::None
        }
    };
}
