# vip-8
![pipeline](https://github.com/github/docs/actions/workflows/pipeline.yml/badge.svg)

Vip-8 is a Chip-8 emulator written in Clojure which has modes based on Vim. I wanted to see if it would be possible to write an emulator using functional programming keeping global mutable state to a minimum. I also wanted to learn a bit of assembler without having to write it myself. 

I'd like to thank [Thomas V. Langhoff](https://github.com/tobiasvl) since he wrote a comprehensive [guide](https://tobiasvl.github.io/blog/write-a-chip-8-emulator/) on how the Chip 8 works which helped me write it. 

If you read the guide, some instructions have different behaviours depending on the machine. This emulator always opts for the modern behaviour. 

## Usage

You can download the jar from the releases page or you can clone the repo and build it yourself. 

`lein uberjar`

Alternatively, you can run it
`lein run`

Or run it inside a repl
`lein repl`

and then
`(-main)`

You can pass the name of the ROM you want to load as a parameter when you execute the program or you can enter
`:load rom`

Bear in mind that there is no autocompletion, since this is meant to be an exercise more than an application to be maintained.

Inside the emulator, you can use `:q` to exit `:pause` to pause and `:run` to keep running the currently loaded ROM. 

The keyboard of the Chip-8 is hardcoded as follows

|   |   |   |   |
|---|---|---|---|
| 1 | 2 | 3 | 4 |
| Q | W | E | R |
| A | S | D | F |
| Z | X | C | V |

Which map to

|   |   |   |   |
|---|---|---|---|
| 1 | 2 | 3 | C |
| 4 | 5 | 6 | D |
| 7 | 8 | 9 | E |
| A | 0 | B | F |

## Contributing
As I said, this is an exercise more than an application to be maintained but if you have an issue, you can report it in the repo and I will look at it. 
Feel free to make a pull request if you have some code to contribute. I just ask that any code for me to review comes with thorough testing at all levels of the pyramid. New code that is not properly tested will not be merged into the main repo. 


## License

Copyright © 2021 Pablo Sánchez Alonso

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
