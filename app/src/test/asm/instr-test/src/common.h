.section text_end

.globl _halt
_halt:
# Magic number，用以识别结束
.word 0x0000006b

# include 位置应该是开头，设置entry和_start符号
.section entry
.globl _start
_start:
