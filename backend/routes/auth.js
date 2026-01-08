const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcryptjs');
const { OAuth2Client } = require('google-auth-library');

// Registration
router.post('/register', async (req, res) => {
    console.log("REGISTER BODY:", req.body);
    const { username, email, password } = req.body;

    if(!username || !password || !email) {
        return res.status(400).json({ message: 'Jardin: Username, email and password are required' });
    }

    try {
        const existingUser = await User.findOne({ email });
        if(existingUser) {
            return res.status(400).json({ message: 'Jardin: User already exists' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const newUser = new User({ username, email, password: hashedPassword });
        await newUser.save();

        res.status(201).json({ message: 'Jardin: User created' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Jardin: Server error' });
    }
});

// Login
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    // 1. Check if user exists
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(401).json({ message: 'Jardin: Invalid email or password' });
    }

    // 2. Password comparision
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(401).json({ message: 'Jardin: Invalid email or password' });
    }

    // 3. Success
    res.status(200).json({
      message: 'Jardin: Login successful',
      userId: user._id,
      email: user.email,
      username: user.username,
    });
  } catch (error) {
    res.status(500).json({ message: 'Jardin: Server error' });
  }
});

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

router.post('/google', async (req, res) => {
  const { idToken } = req.body;

  if (!idToken) {
    return res.status(400).json({ message: 'Missing idToken' });
  }

  try {
    const ticket = await client.verifyIdToken({
      idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });

    const payload = ticket.getPayload();

    const email = payload.email;
    const username = payload.name;
    const googleId = payload.sub;

    let user = await User.findOne({ email });

    if (!user) {
      user = new User({
        username,
        email,
        password: null,
        provider: 'google',
        googleId,
      });

      await user.save();
    }

    res.status(200).json({
      message: 'Jardin: Google login successful',
      email: user.email,
      username: user.username,
      userId: user._id,
    });

  } catch (error) {
    console.error(error);
    res.status(401).json({ message: 'Invalid Google token' });
  }
});

module.exports = router;
