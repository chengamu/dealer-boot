import { writeFileSync, existsSync, mkdirSync } from 'fs';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';
import { loadEnv } from 'vite';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

/**
 * 生成运行时配置文件
 * @param {string} mode 构建模式
 * @param {string} outputDir 输出目录
 */
export function generateAppConfig(mode = 'production', outputDir = 'docs') {
    // 加载环境变量
    const env = loadEnv(mode, process.cwd());

    // 提取以 VITE_APP_ 开头的环境变量
    const appConfig = {};
    Object.keys(env).forEach(key => {
        if (key.startsWith('VITE_APP_') || key.startsWith('VITE_BUILD_')) {
            appConfig[key] = env[key];
        }
    });

    // 生成配置文件内容
    const configContent = `/**
 * 运行时配置文件 - 自动生成
 * 构建时间: ${new Date().toLocaleString()}
 * 构建模式: ${mode}
 * 
 * 此文件在构建后可以直接修改，无需重新构建项目
 * 配置项会覆盖编译时的环境变量
 */
window.__APP_CONFIG__ = ${JSON.stringify(appConfig, null, 2)};`;

    // 确保输出目录存在
    const outputPath = resolve(process.cwd(), outputDir);
    if (!existsSync(outputPath)) {
        mkdirSync(outputPath, { recursive: true });
    }

    // 写入配置文件
    const configFilePath = resolve(outputPath, '_app.config.js');
    writeFileSync(configFilePath, configContent);

    console.log(`✅ 运行时配置文件已生成: ${configFilePath}`);
    console.log(`📝 配置内容:`, appConfig);

    return configFilePath;
}

// 如果作为脚本直接运行
if (import.meta.url === `file://${process.argv[1]}`) {
    const mode = process.argv[2] || 'production';
    const outputDir = process.argv[3] || 'docs';
    generateAppConfig(mode, outputDir);
}