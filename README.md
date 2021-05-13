# LockUp
## _An Android-based Cellebrite UFED self-defense application_

LockUp is an Android application that will monitor the device for signs for attempts to image it using known forensic tools like the Cellebrite UFED. Here is a [blog](https://blog.korelogic.com/blog/2020/06/29/cellebrite_good_times_come_on) I wrote.

- Proof-of-Concept. Not meant as an in-depth defense
- Android API 28, Does not require root
- Relies on RECEIVE_BOOT_COMPLETED to start a Service and AccessibilityService
- Monitors USB events through ACTION_USB_DEVICE, package installations, and known exploit staging locations on the filesystem
- Detects Logical Extractions, File System Extractions, and Physical Extractions leveraging ADB
- Will automatically respond with a factory reset with DeviceAdminReceiver
- Beginning steps to researching more robust anti-forensic techniques

## Signature Detection

- Exploit staging directories and known filenames
- Known file hashes
- Application names and certificate metadata

## TODO Signatures

- Binary-level identifiers
- Hardcoded RSA keys used for ADB authentication (requires root)

## Installation

I avoided including everything needed to build LockUp, making this application so accessible that it may be easily used to avoid criminal prosecution was not my goal. Instead, my goal was to help support my research into forensic tools in showing how they aren't immune to software issues. 

## Author

Matt Bergin, [KoreLogic](https://www.korelogic.com/)

## History

Most recently I [presented](https://www.blackhat.com/asia-21/briefings/schedule/index.html#anti-forensics-reverse-engineering-a-leading-phone-forensic-tool-21789) my research at Blackhat Asia 2021.

I've released security advisories for the Cellebrite UFED which you may also be interested in:

- [KL-001-2020-003: Cellebrite EPR Decryption Relies on Hardcoded AES Key Material](https://korelogic.com/Resources/Advisories/KL-001-2020-003.txt)
- [KL-001-2020-002: Cellebrite Restricted Desktop Escape and Escalation of User Privilege](https://korelogic.com/Resources/Advisories/KL-001-2020-002.txt)
- [KL-001-2020-001: Cellebrite Hardcoded ADB Authentication Keys](https://korelogic.com/Resources/Advisories/KL-001-2020-001.txt)

## License

[Creative Commons Zero 1.0](https://github.com/mbkore/lockup/blob/main/LICENSE)


