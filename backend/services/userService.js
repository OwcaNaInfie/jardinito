const path = require('path');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');
const userRepository = require('../repositories/userRepository');
const verificationTokenRepository = require('../repositories/verificationTokenRepository');
const { sendVerificationEmail } = require('../utils/emailService');

exports.uploadAvatar = async (userId, fileBuffer) => {
    const user = await userRepository.findById(userId);
    if (!user) return null;

    if (user.avatar?.custom) {
        const oldPath = path.join(__dirname, '../public/avatars', user.avatar.custom);
        if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
    }

    const filename = `custom_${uuidv4()}.jpg`;
    const outputPath = path.join(__dirname, '../public/avatars', filename);
    fs.writeFileSync(outputPath, fileBuffer);

    user.avatar.custom = filename;
    await userRepository.save(user);
    return user.avatar;
};

exports.deleteAvatar = async (userId) => {
    const user = await userRepository.findById(userId);
    if (!user) return null;

    if (user.avatar?.custom) {
        const oldPath = path.join(__dirname, '../public/avatars', user.avatar.custom);
        if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
        user.avatar.custom = null;
        await userRepository.save(user);
    }

    return user.avatar;
};

exports.updateUsername = async (userId, username) => {
    const existing = await userRepository.findByUsername(username);
    if (existing && existing._id.toString() !== userId)
        throw new Error('USERNAME_TAKEN');

    const user = await userRepository.findByIdAndUpdate(userId, { username });
    return user?.username ?? null;
};

exports.requestEmailChange = async (userId, newEmail) => {
    const existing = await userRepository.findByEmail(newEmail);
    if (existing) throw new Error('EMAIL_TAKEN');

    const user = await userRepository.findById(userId);
    if (!user) return null;

    const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();

    await verificationTokenRepository.upsert(userId, 'email_change', {
        code: verificationCode,
        codeExpiry: new Date(Date.now() + 2 * 60 * 1000),
        newEmail
    });

    await sendVerificationEmail(newEmail, verificationCode);
    return true;
};

exports.confirmEmailChange = async (userId, code) => {
    const token = await verificationTokenRepository.findByUserIdAndType(userId, 'email_change');
    if (!token) throw new Error('TOKEN_NOT_FOUND');
    if (new Date() > token.codeExpiry) throw new Error('CODE_EXPIRED');
    if (token.code !== code) throw new Error('INVALID_CODE');

    const user = await userRepository.findByIdAndUpdate(userId, { email: token.newEmail });
    await verificationTokenRepository.deleteById(token._id);
    return user.email;
};

exports.deleteAccount = async (userId) => {
    const user = await userRepository.findById(userId);
    if (!user) return null;

    if (user.avatar?.custom) {
        const oldPath = path.join(__dirname, '../public/avatars', user.avatar.custom);
        if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
    }

    await userRepository.findByIdAndDelete(userId);
    console.log(`Account deleted: ${userId}`);
    return true;
};