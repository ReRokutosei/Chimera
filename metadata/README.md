# F-Droid Metadata & Publication Notice

## 关于 F-Droid 上架计划的中止说明 (Notice on F-Droid Submission)

由于 GitLab 平台目前要求新用户提供个人手机号码并填写信用卡 CVV 等敏感金融信息以激活 CI/CD 流水线，出于对个人隐私与敏感信息安全性的高度考量与不信任，本项目决定**无限期中止向上架 F-Droid 官方主仓库（fdroiddata）的提交计划**，除非后续验证策略发生改变或有特殊情况。

Due to GitLab requiring personal phone numbers and credit card CVV details to enable CI pipelines for Merge Requests, the maintainer has decided to **indefinitely suspend official F-Droid (fdroiddata) submission** over privacy and financial data security concerns, unless verification policies change.

---

## 备份说明 (Backup & Reference)

本目录与仓库根目录下的 `fastlane/metadata/android/` 完整保留了 F-Droid 与 Fastlane 规范所需的元数据配方与多语言商店素材，可供第三方仓库索引、自建 F-Droid 仓库（F-Droid Repo）或后续参考：

- [`com.rerokutosei.chimera.yml`](./com.rerokutosei.chimera.yml)：包含可复现构建（Reproducible Builds）与自动更新检测规则的 F-Droid 元数据配方定义文件。
- `../fastlane/metadata/android/`：包含多语言描述、应用图标、更新日志及手机/平板截图素材。
