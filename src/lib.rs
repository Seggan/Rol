pub mod parsing;
pub mod common;
pub mod error;

#[macro_export]
macro_rules! try_consume {
    ($toks:expr, $tok:pat, $err:literal) => {
        if let Some(next) = $toks.next() {
            match next.token_type {
                $tok => next,
                _ => return Result::Err($crate::error::SyntaxError::ExpectedToken(
                    $err.to_string(),
                    next.position
                ))
            }
        } else {
            return Result::Err($crate::error::SyntaxError::UnexpectedEof);
        }
    };
}