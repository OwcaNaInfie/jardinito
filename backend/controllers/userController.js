const userService = require('../services/userService');

exports.uploadAvatar = async (req, res) => {
    try {
        if (!req.file) return res.status(400).json({ message: 'No file uploaded' });
        const { userId } = req.body;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const avatar = await userService.uploadAvatar(userId, req.file.buffer);
        if (!avatar) return res.status(404).json({ message: 'User not found' });

        res.json({ avatar });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Avatar upload failed' });
    }
};

exports.deleteAvatar = async (req, res) => {
    try {
        const { userId } = req.body;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const avatar = await userService.deleteAvatar(userId);
        if (!avatar) return res.status(404).json({ message: 'User not found' });

        res.json({ avatar });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Delete avatar failed' });
    }
};

exports.updateUsername = async (req, res) => {
    try {
        const { userId, username } = req.body;
        if (!userId || !username)
            return res.status(400).json({ message: 'userId and username are required' });
        if (username.length > 20)
            return res.status(422).json({ message: 'Username cannot exceed 20 characters' });

        const updatedUsername = await userService.updateUsername(userId, username);
        if (updatedUsername === null) return res.status(404).json({ message: 'User not found' });

        res.json({ message: 'Username updated successfully', username: updatedUsername });
    } catch (err) {
        if (err.message === 'USERNAME_TAKEN')
            return res.status(409).json({ message: 'Username already taken' });
        console.error(err);
        res.status(500).json({ message: 'Update username failed' });
    }
};

exports.requestEmailChange = async (req, res) => {
    try {
        const { userId, newEmail } = req.body;
        if (!userId || !newEmail)
            return res.status(400).json({ message: 'userId and newEmail are required' });

        const result = await userService.requestEmailChange(userId, newEmail);
        if (!result) return res.status(404).json({ message: 'User not found' });

        res.json({ message: 'Verification code sent to new email' });
    } catch (err) {
        if (err.message === 'EMAIL_TAKEN')
            return res.status(409).json({ message: 'Email already taken' });
        console.error(err);
        res.status(500).json({ message: 'Request email change failed' });
    }
};

exports.confirmEmailChange = async (req, res) => {
    try {
        const { userId, code } = req.body;
        if (!userId || !code)
            return res.status(400).json({ message: 'userId and code are required' });

        const email = await userService.confirmEmailChange(userId, code);
        res.json({ message: 'Email updated successfully', email });
    } catch (err) {
        if (err.message === 'TOKEN_NOT_FOUND')
            return res.status(404).json({ message: 'Token not found' });
        if (err.message === 'CODE_EXPIRED')
            return res.status(410).json({ message: 'Code expired' });
        if (err.message === 'INVALID_CODE')
            return res.status(400).json({ message: 'Invalid code' });
        console.error(err);
        res.status(500).json({ message: 'Confirm email change failed' });
    }
};

exports.deleteAccount = async (req, res) => {
    try {
        const { userId } = req.body;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const result = await userService.deleteAccount(userId);
        if (!result) return res.status(404).json({ message: 'User not found' });

        res.json({ message: 'Account deleted successfully' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Delete account failed' });
    }
};