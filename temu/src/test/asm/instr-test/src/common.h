.section text_end

.globl _halt
_halt:
ebreak

# include 位置应该是开头，设置entry和_start符号
.section entry
.globl _start
_start:
