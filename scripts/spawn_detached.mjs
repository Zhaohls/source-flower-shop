import { openSync } from 'node:fs'
import { spawn } from 'node:child_process'

const args = process.argv.slice(2)
const separator = args.indexOf('--')
if (separator < 0) throw new Error('缺少命令分隔符 --')

let cwd = ''
let log = ''
for (let index = 0; index < separator; index += 2) {
  if (args[index] === '--cwd') cwd = args[index + 1]
  if (args[index] === '--log') log = args[index + 1]
}
const command = args.slice(separator + 1)
if (!cwd || !log || command.length === 0) throw new Error('必须提供工作目录、日志和命令')

const logFd = openSync(log, 'a')
const child = spawn(command[0], command.slice(1), {
  cwd,
  detached: true,
  env: process.env,
  stdio: ['ignore', logFd, logFd],
})
child.unref()
process.stdout.write(`${child.pid}\n`)
