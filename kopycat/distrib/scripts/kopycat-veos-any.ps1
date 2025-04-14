param (
  [Parameter(Mandatory, Position=0)][string]$arch,
  [Parameter(Position=1)][string]$exec,
  [Parameter(Position=2, ValueFromRemainingArguments=$true)][string[]]$remargs
)

function Die {
  param ([Parameter(Position=0)][string]$err)
  write-error $err
  exit 1
}

if (!$exec) {
  Die "Input executable as first argument isn't specified"
}

switch ($arch) {
  "arm" {
    $script = "veos-arm.ps1"
  }
  "mips" {
    $script = "veos-mips.ps1"
  }
  default {
    Die "Unknown architecture for VEOS: $arch"
  }
}

if ($Env:KOPYCAT_VEOS_GDB_PORT) {
  $gdb = "-g $Env:KOPYCAT_VEOS_GDB_PORT"
} else {
  $gdb = ""
}

if ($Env:KOPYCAT_VEOS_CONSOLE) {
  $console_type = ($Env:KOPYCAT_VEOS_CONSOLE -split "=")[0]

  switch ($console_type) {
    "kotlin" {
      $console = "-kts"
    }
    "python" {
      $console = "-$Env:KOPYCAT_VEOS_CONSOLE"
    }
    default {
      Die "Unknown console type, use 'kotlin' or 'python=<command>'"
    }
  }
} else {
  $console = "-kts"
}

if ($Env:KOPYCAT_VEOS_WORKING_DIR) {
  $working_dir = "dir=$Env:KOPYCAT_VEOS_WORKING_DIR"
} else {
  $working_dir = "dir=$PWD"
}

if ($Env:KOPYCAT_VEOS_LD_PRELOAD) {
  $ld_preload = ",ldPreload=$Env:KOPYCAT_VEOS_LD_PRELOAD"
} else {
  $ld_preload = ""
}

if ($Env:KOPYCAT_VEOS_STANDALONE -eq "NO") {
  $standalone = ""
} else {
  $standalone = "-standalone"
}

if ($Env:KOPYCAT_VEOS_LOGGING_CONF) {
  $logging_conf = "-ll $Env:KOPYCAT_VEOS_LOGGING_CONF"
} else {
  $logging_conf = ""
}

$oldwd = $PWD
try {
  & "$PSScriptRoot\..\..\..\temp\config\powershell\$script" $standalone $gdb $console -p "$working_dir,exec=$exec,args=$remargs$ld_preload" $logging_conf
} finally {
  cd $oldwd
}
