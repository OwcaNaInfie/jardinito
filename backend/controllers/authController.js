const authService = require('../services/authService');

exports.checkUsername = async (req, res) => {
    try {
        const { username } = req.query;
        if (!username) return res.status(400).json({ message: 'Username query param is required' });
        const usernameAvailable = await authService.checkUsername(username);
        res.status(200).json({ usernameAvailable });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.checkEmail = async (req, res) => {
    try {
        const { email } = req.query;
        if (!email) return res.status(400).json({ message: 'Email query param is required' });
        const emailAvailable = await authService.checkEmail(email);
        res.status(200).json({ emailAvailable });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.register = async (req, res) => {
    console.log('REGISTER BODY:', req.body);
    try {
        const { username, email, password } = req.body;
        if (!username || !password || !email)
            return res.status(400).json({ message: 'Username, email and password are required' });
        if (username.length > 20)
            return res.status(400).json({ message: 'Username cannot exceed 20 characters' });

        const result = await authService.register(username, email, password);
        res.status(201).json(result);
    } catch (err) {
        if (err.message === 'USER_EXISTS')
            return res.status(400).json({ message: 'User already exists' });
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.login = async (req, res) => {
    try {
        const { identifier, password } = req.body;
        const result = await authService.login(identifier, password);
        res.status(200).json(result);
    } catch (err) {
        if (err.message === 'INVALID_CREDENTIALS')
            return res.status(401).json({ message: 'Invalid credentials' });
        if (err.message === 'NOT_VERIFIED')
            return res.status(403).json({
                message: 'Email not verified',
                userId: err.userId,
                email: err.email
            });
        console.error('LOGIN ERROR:', err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.googleLogin = async (req, res) => {
    try {
        const { idToken } = req.body;
        const result = await authService.googleLogin(idToken);
        res.status(200).json(result);
    } catch (err) {
        if (err.message === 'INVALID_GOOGLE_TOKEN')
            return res.status(400).json({ message: 'Invalid Google token' });
        console.error(err);
        res.status(401).json({ message: 'Google authentication failed' });
    }
};

exports.verifyEmail = async (req, res) => {
    try {
        const { userId, code } = req.body;
        if (!userId || !code)
            return res.status(400).json({ message: 'userId and code are required' });

        const result = await authService.verifyEmail(userId, code);
        res.status(200).json(result);
    } catch (err) {
        if (err.message === 'USER_NOT_FOUND')
            return res.status(404).json({ message: 'User not found' });
        if (err.message === 'ALREADY_VERIFIED')
            return res.status(400).json({ message: 'Account already verified' });
        if (err.message === 'TOKEN_NOT_FOUND')
            return res.status(404).json({ message: 'Verification token not found' });
        if (err.message === 'CODE_EXPIRED')
            return res.status(410).json({ message: 'Code expired' });
        if (err.message === 'INVALID_CODE')
            return res.status(400).json({ message: 'Invalid code' });
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.resendVerification = async (req, res) => {
    try {
        const { userId } = req.body;
        await authService.resendVerification(userId);
        res.status(200).json({ message: 'Verification code resent' });
    } catch (err) {
        if (err.message === 'USER_NOT_FOUND')
            return res.status(404).json({ message: 'User not found' });
        if (err.message === 'ALREADY_VERIFIED')
            return res.status(400).json({ message: 'Account already verified' });
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.getUserId = async (req, res) => {
    try {
        const { identifier } = req.body;
        const result = await authService.getUserId(identifier);
        res.status(200).json(result);
    } catch (err) {
        if (err.message === 'USER_NOT_FOUND')
            return res.status(404).json({ message: 'User not found' });
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.forgotPassword = async (req, res) => {
    try {
        const { identifier } = req.body;
        if (!identifier)
            return res.status(422).json({ message: 'Email or username is required' });

        const result = await authService.forgotPassword(identifier);
        if (!result) return res.status(200).json({ message: 'If account exists, code will be sent' });

        res.status(200).json({ message: 'Reset code sent', userId: result.userId });
    } catch (err) {
        if (err.message === 'NOT_VERIFIED_RESET')
            return res.status(403).json({ message: 'Account not verified' });
        if (err.message === 'GOOGLE_ACCOUNT')
            return res.status(400).json({ message: 'Google account cannot reset password' });
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.resetPassword = async (req, res) => {
    try {
        const { userId, code, newPassword } = req.body;
        if (!userId || !code || !newPassword)
            return res.status(400).json({ message: 'userId, code and newPassword are required' });

        await authService.resetPassword(userId, code, newPassword);
        res.status(200).json({ message: 'Password reset successfully' });
    } catch (err) {
        if (err.message === 'TOKEN_NOT_FOUND')
            return res.status(404).json({ message: 'Reset token not found' });
        if (err.message === 'CODE_EXPIRED')
            return res.status(410).json({ message: 'Code expired' });
        if (err.message === 'INVALID_CODE')
            return res.status(400).json({ message: 'Invalid code' });
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};