// Finance Concierge Chat Application
const API_BASE_URL = '/api/chat';
let isLoading = false;

// DOM Elements
const chatMessages = document.getElementById('chatMessages');
const chatForm = document.getElementById('chatForm');
const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendBtn');
const resetBtn = document.getElementById('resetBtn');
const quickActionBtns = document.querySelectorAll('.quick-action-btn');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    messageInput.focus();
});

function setupEventListeners() {
    // Chat form submission
    chatForm.addEventListener('submit', handleSubmit);

    // Reset button
    resetBtn.addEventListener('click', handleReset);

    // Quick action buttons
    quickActionBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const message = btn.getAttribute('data-message');
            if (message) {
                messageInput.value = message;
                handleSubmit(new Event('submit'));
            }
        });
    });

    // Enter key to send (Shift+Enter for new line)
    messageInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            chatForm.dispatchEvent(new Event('submit'));
        }
    });
}

async function handleSubmit(e) {
    e.preventDefault();

    if (isLoading) return;

    const message = messageInput.value.trim();
    if (!message) return;

    // Add user message to chat
    addMessage(message, 'user');

    // Clear input
    messageInput.value = '';

    // Show typing indicator
    const typingIndicator = showTypingIndicator();

    // Disable input
    setLoading(true);

    try {
        const response = await sendMessageToAgent(message);

        // Remove typing indicator
        removeTypingIndicator(typingIndicator);

        // Add bot response
        addMessage(response, 'bot');

    } catch (error) {
        console.error('Error:', error);
        removeTypingIndicator(typingIndicator);
        addMessage('Sorry, I encountered an error processing your request. Please try again.', 'bot', true);
    } finally {
        setLoading(false);
        messageInput.focus();
    }
}

async function sendMessageToAgent(message) {
    const response = await fetch(`${API_BASE_URL}/message`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            message: message,
            userId: 'web-user'
        })
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Handle Server-Sent Events stream
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let fullResponse = '';

    while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        fullResponse += chunk;
    }

    return fullResponse.trim() || 'No response from agent.';
}

async function handleReset() {
    if (!confirm('Are you sure you want to reset the conversation?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/reset`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: 'web-user'
            })
        });

        if (response.ok) {
            // Clear chat messages except the welcome message
            const welcomeMessage = chatMessages.querySelector('.bot-message');
            chatMessages.innerHTML = '';
            if (welcomeMessage) {
                chatMessages.appendChild(welcomeMessage.cloneNode(true));
            }

            showNotification('Session reset successfully!', 'success');
        }
    } catch (error) {
        console.error('Error resetting session:', error);
        showNotification('Failed to reset session', 'error');
    }
}

function addMessage(text, sender = 'bot', isError = false) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}-message`;

    const avatar = document.createElement('div');
    avatar.className = 'message-avatar';
    avatar.textContent = sender === 'user' ? '👤' : '🤖';

    const content = document.createElement('div');
    content.className = 'message-content';

    if (isError) {
        content.style.background = '#fee2e2';
        content.style.color = '#991b1b';
    }

    // Format the message (preserve line breaks and basic formatting)
    const formattedText = formatMessage(text);
    content.innerHTML = formattedText;

    messageDiv.appendChild(avatar);
    messageDiv.appendChild(content);

    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

function formatMessage(text) {
    // Convert newlines to <br>
    let formatted = text.replace(/\n/g, '<br>');

    // Convert markdown-style bold
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

    // Convert markdown-style lists
    formatted = formatted.replace(/^- (.+)$/gm, '<li>$1</li>');
    if (formatted.includes('<li>')) {
        formatted = formatted.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');
    }

    // Convert dollar amounts to highlighted spans
    formatted = formatted.replace(/\$(\d+(?:\.\d{2})?)/g, '<strong style="color: #10b981;">$$$1</strong>');

    return formatted;
}

function showTypingIndicator() {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message bot-message typing-message';
    messageDiv.id = 'typing-indicator';

    const avatar = document.createElement('div');
    avatar.className = 'message-avatar';
    avatar.textContent = '🤖';

    const content = document.createElement('div');
    content.className = 'message-content';

    const typingDiv = document.createElement('div');
    typingDiv.className = 'typing-indicator';
    typingDiv.innerHTML = '<span></span><span></span><span></span>';

    content.appendChild(typingDiv);
    messageDiv.appendChild(avatar);
    messageDiv.appendChild(content);

    chatMessages.appendChild(messageDiv);
    scrollToBottom();

    return messageDiv;
}

function removeTypingIndicator(indicator) {
    if (indicator && indicator.parentNode) {
        indicator.parentNode.removeChild(indicator);
    }
}

function setLoading(loading) {
    isLoading = loading;
    sendBtn.disabled = loading;
    messageInput.disabled = loading;

    if (loading) {
        sendBtn.classList.add('loading');
    } else {
        sendBtn.classList.remove('loading');
    }
}

function scrollToBottom() {
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function showNotification(message, type = 'info') {
    // Create a simple notification
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 16px 24px;
        background: ${type === 'success' ? '#10b981' : '#ef4444'};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Check API health on load
async function checkApiHealth() {
    try {
        const response = await fetch(`${API_BASE_URL}/health`);
        const data = await response.json();
        console.log('API Health:', data);
    } catch (error) {
        console.error('API health check failed:', error);
        showNotification('Warning: Could not connect to backend API', 'error');
    }
}

// Run health check
checkApiHealth();

